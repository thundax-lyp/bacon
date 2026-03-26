package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderItemDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.command.CreateOrderItemCommand;
import com.github.thundax.bacon.order.application.command.CreateOrderCommand;
import com.github.thundax.bacon.order.application.query.GetOrderQuery;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderDomainService;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class OrderApplicationService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String CLOSE_REASON_INVENTORY_RESERVE_FAILED = "INVENTORY_RESERVE_FAILED";
    private static final String CLOSE_REASON_PAYMENT_CREATE_FAILED = "PAYMENT_CREATE_FAILED";

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService = new OrderDomainService();
    private final OrderNoGenerator orderNoGenerator;
    private final InventoryCommandFacade inventoryCommandFacade;
    private final PaymentCommandFacade paymentCommandFacade;

    public OrderApplicationService(OrderRepository orderRepository, OrderNoGenerator orderNoGenerator,
                                   InventoryCommandFacade inventoryCommandFacade,
                                   PaymentCommandFacade paymentCommandFacade) {
        this.orderRepository = orderRepository;
        this.orderNoGenerator = orderNoGenerator;
        this.inventoryCommandFacade = inventoryCommandFacade;
        this.paymentCommandFacade = paymentCommandFacade;
    }

    public OrderSummaryDTO create(CreateOrderCommand command) {
        if (command.tenantId() == null || command.userId() == null) {
            throw new IllegalArgumentException("tenantId and userId are required");
        }
        List<CreateOrderItemCommand> items = command.items() == null ? List.of() : command.items();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        BigDecimal totalAmount = items.stream()
                .map(this::calculateLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String orderNo = orderNoGenerator.nextOrderNo();
        Order order = orderDomainService.create(null, command.tenantId(), orderNo, command.userId(),
                resolveCurrencyCode(command.currencyCode()), totalAmount, totalAmount, command.remark(),
                command.expiredAt());
        Order savedOrder = orderRepository.save(order);
        orderRepository.saveItems(savedOrder.getTenantId(), savedOrder.getId(), items.stream()
                .map(item -> new OrderItem(savedOrder.getTenantId(), savedOrder.getId(), item.skuId(), item.skuName(),
                        item.quantity(), item.salePrice(), calculateLineAmount(item)))
                .toList());
        savedOrder.markReservingStock();
        orderRepository.save(savedOrder);

        InventoryReservationResultDTO reserveResult = reserveInventory(savedOrder, items);
        savedOrder.markInventoryReserved(reserveResult.getReservationNo(), reserveResult.getWarehouseId());
        orderRepository.save(savedOrder);

        PaymentCreateResultDTO paymentResult = createPayment(savedOrder, command.channelCode());
        savedOrder.markPendingPayment(paymentResult.getPaymentNo(), paymentResult.getChannelCode());
        orderRepository.save(savedOrder);
        return toSummary(savedOrder);
    }

    public OrderDetailDTO get(GetOrderQuery query) {
        return orderRepository.findById(query.orderId())
                .map(this::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + query.orderId()));
    }

    public OrderDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return orderRepository.findByOrderNo(tenantId, orderNo)
                .map(this::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
    }

    public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize(), DEFAULT_PAGE_SIZE);
        List<Order> filtered = orderRepository.findAll().stream()
                .filter(order -> query.getTenantId() == null || query.getTenantId().equals(order.getTenantId()))
                .filter(order -> query.getUserId() == null || query.getUserId().equals(order.getUserId()))
                .filter(order -> query.getOrderNo() == null || order.getOrderNo().contains(query.getOrderNo()))
                .toList();
        filtered = filtered.stream()
                .filter(order -> query.getOrderStatus() == null || query.getOrderStatus().equals(order.getOrderStatus()))
                .filter(order -> query.getPayStatus() == null || query.getPayStatus().equals(order.getPayStatus()))
                .filter(order -> query.getInventoryStatus() == null
                        || query.getInventoryStatus().equals(order.getInventoryStatus()))
                .filter(order -> query.getCreatedAtFrom() == null || !order.getCreatedAt().isBefore(query.getCreatedAtFrom()))
                .filter(order -> query.getCreatedAtTo() == null || !order.getCreatedAt().isAfter(query.getCreatedAtTo()))
                .sorted(java.util.Comparator.comparing(Order::getCreatedAt).reversed().thenComparing(Order::getId).reversed())
                .toList();
        long total = filtered.size();
        List<OrderSummaryDTO> records = filtered.stream()
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .map(this::toSummary)
                .toList();
        return new OrderPageResultDTO(records, total, pageNo, pageSize);
    }

    public void cancelOrder(Long tenantId, String orderNo, String reason) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        order.cancel(reason);
        InventoryReservationResultDTO releaseResult = inventoryCommandFacade.releaseReservedStock(tenantId, orderNo, reason);
        applyReleaseResult(order, releaseResult, reason);
        if (order.getPaymentNo() != null && !order.getPaymentNo().isBlank()) {
            paymentCommandFacade.closePayment(tenantId, order.getPaymentNo(), reason);
        }
        orderRepository.save(order);
    }

    public void markPaid(Long tenantId, String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount,
                         Instant paidTime) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        order.markPaid(paymentNo, channelCode, paidAmount, paidTime);
        InventoryReservationResultDTO deductResult = inventoryCommandFacade.deductReservedStock(tenantId, orderNo);
        if (!Order.INVENTORY_STATUS_DEDUCTED.equals(deductResult.getInventoryStatus())) {
            String reason = resolveFailureReason(deductResult.getFailureReason(), "inventory deduct failed");
            order.markInventoryFailed(deductResult.getReservationNo(), deductResult.getWarehouseId(), reason);
            orderRepository.save(order);
            throw new IllegalStateException(reason);
        }
        order.markInventoryDeducted(deductResult.getReservationNo(), deductResult.getWarehouseId(),
                deductResult.getDeductedAt());
        orderRepository.save(order);
    }

    public void markPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason, String channelStatus,
                                  Instant failedTime) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        order.markPaymentFailed(paymentNo, reason, channelStatus, failedTime);
        InventoryReservationResultDTO releaseResult =
                inventoryCommandFacade.releaseReservedStock(tenantId, orderNo, "PAYMENT_FAILED");
        applyReleaseResult(order, releaseResult, "PAYMENT_FAILED");
        orderRepository.save(order);
    }

    public void closeExpiredOrder(Long tenantId, String orderNo, String reason) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        order.closeExpired(reason);
        if (order.getPaymentNo() != null && !order.getPaymentNo().isBlank()) {
            paymentCommandFacade.closePayment(tenantId, order.getPaymentNo(), reason);
        }
        InventoryReservationResultDTO releaseResult = inventoryCommandFacade.releaseReservedStock(tenantId, orderNo, reason);
        applyReleaseResult(order, releaseResult, reason);
        orderRepository.save(order);
    }

    private InventoryReservationResultDTO reserveInventory(Order order, List<CreateOrderItemCommand> items) {
        List<InventoryReservationItemDTO> reserveItems = items.stream()
                .map(item -> new InventoryReservationItemDTO(item.skuId(), item.quantity()))
                .toList();
        InventoryReservationResultDTO reserveResult = inventoryCommandFacade.reserveStock(
                order.getTenantId(), order.getOrderNo(), reserveItems);
        if (!Order.INVENTORY_STATUS_RESERVED.equals(reserveResult.getInventoryStatus())) {
            String reason = resolveFailureReason(reserveResult.getFailureReason(), "inventory reserve failed");
            order.markInventoryFailed(reserveResult.getReservationNo(), reserveResult.getWarehouseId(), reason);
            order.closeByInventoryReserveFailed(CLOSE_REASON_INVENTORY_RESERVE_FAILED);
            orderRepository.save(order);
            throw new IllegalStateException(reason);
        }
        return reserveResult;
    }

    private PaymentCreateResultDTO createPayment(Order order, String channelCode) {
        PaymentCreateResultDTO paymentResult = paymentCommandFacade.createPayment(order.getTenantId(), order.getOrderNo(),
                order.getUserId(), order.getPayableAmount(), channelCode, buildPaymentSubject(order), order.getExpiredAt());
        if (paymentResult.getPaymentNo() == null || paymentResult.getPaymentNo().isBlank()) {
            closeForPaymentCreateFailed(order, resolveFailureReason(paymentResult.getFailureReason(), "payment create failed"));
        }
        if (!Order.PAY_STATUS_PAYING.equals(paymentResult.getPaymentStatus())) {
            closeForPaymentCreateFailed(order, resolveFailureReason(paymentResult.getFailureReason(), "payment not in PAYING"));
        }
        return paymentResult;
    }

    private void closeForPaymentCreateFailed(Order order, String failureReason) {
        InventoryReservationResultDTO releaseResult =
                inventoryCommandFacade.releaseReservedStock(order.getTenantId(), order.getOrderNo(), CLOSE_REASON_PAYMENT_CREATE_FAILED);
        applyReleaseResult(order, releaseResult, CLOSE_REASON_PAYMENT_CREATE_FAILED);
        order.closeByPaymentCreateFailed(CLOSE_REASON_PAYMENT_CREATE_FAILED);
        orderRepository.save(order);
        throw new IllegalStateException(failureReason);
    }

    private void applyReleaseResult(Order order, InventoryReservationResultDTO releaseResult, String fallbackReason) {
        if (Order.INVENTORY_STATUS_RELEASED.equals(releaseResult.getInventoryStatus())) {
            order.markInventoryReleased(releaseResult.getReservationNo(), releaseResult.getWarehouseId(),
                    releaseResult.getReleaseReason(), releaseResult.getReleasedAt());
            return;
        }
        order.markInventoryFailed(releaseResult.getReservationNo(), releaseResult.getWarehouseId(),
                resolveFailureReason(releaseResult.getFailureReason(), fallbackReason));
    }

    private String buildPaymentSubject(Order order) {
        return "order:" + order.getOrderNo();
    }

    private String resolveFailureReason(String reason, String defaultReason) {
        return reason == null || reason.isBlank() ? defaultReason : reason;
    }

    private OrderSummaryDTO toSummary(Order order) {
        return new OrderSummaryDTO(order.getId(), order.getTenantId(), order.getOrderNo(), order.getUserId(),
                order.getOrderStatus(), order.getPayStatus(), order.getInventoryStatus(), order.getPaymentNo(),
                order.getReservationNo(), order.getCurrencyCode(), order.getTotalAmount(), order.getPayableAmount(),
                order.getCancelReason(), order.getCloseReason(), order.getCreatedAt(), order.getExpiredAt());
    }

    private OrderDetailDTO toDetail(Order order) {
        List<OrderItemDTO> itemDtos = orderRepository.findItemsByOrderId(order.getTenantId(), order.getId()).stream()
                .map(item -> new OrderItemDTO(item.getSkuId(), item.getSkuName(), item.getQuantity(),
                        item.getSalePrice(), item.getLineAmount()))
                .toList();
        return new OrderDetailDTO(order.getId(), order.getTenantId(), order.getOrderNo(), order.getUserId(),
                order.getOrderStatus(), order.getPayStatus(), order.getInventoryStatus(), order.getPaymentNo(),
                order.getReservationNo(), order.getCurrencyCode(), order.getTotalAmount(), order.getPayableAmount(),
                order.getCancelReason(), order.getCloseReason(), order.getCreatedAt(), order.getExpiredAt(), itemDtos,
                buildPaymentSnapshot(order), buildInventorySnapshot(order), order.getPaidAt(), order.getClosedAt());
    }

    private BigDecimal calculateLineAmount(CreateOrderItemCommand item) {
        if (item == null || item.quantity() == null || item.salePrice() == null) {
            throw new IllegalArgumentException("order item quantity and salePrice are required");
        }
        return item.salePrice().multiply(BigDecimal.valueOf(item.quantity()));
    }

    private String resolveCurrencyCode(String currencyCode) {
        return currencyCode == null || currencyCode.isBlank() ? "CNY" : currencyCode;
    }

    private String buildPaymentSnapshot(Order order) {
        if (order.getPaymentNo() == null) {
            return null;
        }
        return "paymentNo=" + order.getPaymentNo()
                + ",payStatus=" + order.getPayStatus()
                + ",channelCode=" + Objects.toString(order.getPaymentChannelCode(), "N/A")
                + ",paidAmount=" + Objects.toString(order.getPaidAmount(), "N/A")
                + ",channelStatus=" + Objects.toString(order.getPaymentChannelStatus(), "N/A")
                + ",failureReason=" + Objects.toString(order.getPaymentFailureReason(), "N/A");
    }

    private String buildInventorySnapshot(Order order) {
        String reservationNo = Objects.toString(order.getReservationNo(), "N/A");
        return "reservationNo=" + reservationNo
                + ",inventoryStatus=" + order.getInventoryStatus()
                + ",warehouseId=" + Objects.toString(order.getWarehouseId(), "N/A")
                + ",failureReason=" + Objects.toString(order.getInventoryFailureReason(), "N/A");
    }
}
