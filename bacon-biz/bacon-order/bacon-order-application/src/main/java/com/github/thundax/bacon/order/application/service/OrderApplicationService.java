package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
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
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageQuery;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageResult;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderDomainService;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class OrderApplicationService {

    private static final String ACTION_CREATE = "ORDER_CREATE";
    private static final String ACTION_CANCEL = "ORDER_CANCEL";
    private static final String ACTION_MARK_PAID = "ORDER_MARK_PAID";
    private static final String ACTION_MARK_PAYMENT_FAILED = "ORDER_MARK_PAYMENT_FAILED";
    private static final String ACTION_CLOSE_EXPIRED = "ORDER_CLOSE_EXPIRED";

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService = new OrderDomainService();
    private final OrderNoGenerator orderNoGenerator;
    private final InventoryCommandFacade inventoryCommandFacade;
    private final PaymentCommandFacade paymentCommandFacade;
    private final OrderOutboxActionExecutor orderOutboxActionExecutor;

    public OrderApplicationService(OrderRepository orderRepository, OrderNoGenerator orderNoGenerator,
                                   InventoryCommandFacade inventoryCommandFacade,
                                   PaymentCommandFacade paymentCommandFacade,
                                   OrderOutboxActionExecutor orderOutboxActionExecutor) {
        this.orderRepository = orderRepository;
        this.orderNoGenerator = orderNoGenerator;
        this.inventoryCommandFacade = inventoryCommandFacade;
        this.paymentCommandFacade = paymentCommandFacade;
        this.orderOutboxActionExecutor = orderOutboxActionExecutor;
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
        orderOutboxActionExecutor.enqueueReserveStock(savedOrder.getTenantId(), savedOrder.getOrderNo(),
                command.channelCode());
        persistOrderDerivedData(savedOrder, ACTION_CREATE, Order.ORDER_STATUS_CREATED);
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
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        OrderPageResult pageResult = orderRepository.pageOrders(new OrderPageQuery(query.getTenantId(),
                query.getUserId(), query.getOrderNo(), query.getOrderStatus(), query.getPayStatus(),
                query.getInventoryStatus(), query.getCreatedAtFrom(), query.getCreatedAtTo(), offset, pageSize));
        List<OrderSummaryDTO> records = pageResult.records().stream()
                .map(this::toSummary)
                .toList();
        return new OrderPageResultDTO(records, pageResult.total(), pageNo, pageSize);
    }

    public void cancelOrder(Long tenantId, String orderNo, String reason) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        String beforeStatus = order.getOrderStatus();
        order.cancel(reason);
        InventoryReservationResultDTO releaseResult = inventoryCommandFacade.releaseReservedStock(tenantId, orderNo, reason);
        applyReleaseResult(order, releaseResult, reason);
        if (order.getPaymentNo() != null && !order.getPaymentNo().isBlank()) {
            paymentCommandFacade.closePayment(tenantId, order.getPaymentNo(), reason);
        }
        orderRepository.save(order);
        persistOrderDerivedData(order, ACTION_CANCEL, beforeStatus);
    }

    public void markPaid(Long tenantId, String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount,
                         Instant paidTime) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        String beforeStatus = order.getOrderStatus();
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
        persistOrderDerivedData(order, ACTION_MARK_PAID, beforeStatus);
    }

    public void markPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason, String channelStatus,
                                  Instant failedTime) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        String beforeStatus = order.getOrderStatus();
        order.markPaymentFailed(paymentNo, reason, channelStatus, failedTime);
        InventoryReservationResultDTO releaseResult =
                inventoryCommandFacade.releaseReservedStock(tenantId, orderNo, "PAYMENT_FAILED");
        applyReleaseResult(order, releaseResult, "PAYMENT_FAILED");
        orderRepository.save(order);
        persistOrderDerivedData(order, ACTION_MARK_PAYMENT_FAILED, beforeStatus);
    }

    public void closeExpiredOrder(Long tenantId, String orderNo, String reason) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        String beforeStatus = order.getOrderStatus();
        order.closeExpired(reason);
        if (order.getPaymentNo() != null && !order.getPaymentNo().isBlank()) {
            paymentCommandFacade.closePayment(tenantId, order.getPaymentNo(), reason);
        }
        InventoryReservationResultDTO releaseResult = inventoryCommandFacade.releaseReservedStock(tenantId, orderNo, reason);
        applyReleaseResult(order, releaseResult, reason);
        orderRepository.save(order);
        persistOrderDerivedData(order, ACTION_CLOSE_EXPIRED, beforeStatus);
    }

    private void persistOrderDerivedData(Order order, String actionType, String beforeStatus) {
        Instant now = Instant.now();
        if (order.getPaymentNo() != null && !order.getPaymentNo().isBlank()) {
            orderRepository.savePaymentSnapshot(new OrderPaymentSnapshot(null, order.getTenantId(), order.getId(),
                    order.getPaymentNo(), order.getPaymentChannelCode(), order.getPayStatus(), order.getPaidAmount(),
                    order.getPaidAt(), order.getPaymentFailureReason(), order.getPaymentChannelStatus(), now));
        }
        if (order.getReservationNo() != null && !order.getReservationNo().isBlank()) {
            orderRepository.saveInventorySnapshot(new OrderInventorySnapshot(null, order.getTenantId(), order.getId(),
                    order.getReservationNo(), order.getInventoryStatus(), order.getWarehouseId(),
                    order.getInventoryFailureReason(), now));
        }
        orderRepository.saveAuditLog(new OrderAuditLog(null, order.getTenantId(), order.getOrderNo(), actionType,
                beforeStatus, order.getOrderStatus(), "SYSTEM", 0L, now));
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
        OrderPaymentSnapshot paymentSnapshot = orderRepository.findPaymentSnapshotByOrderId(order.getTenantId(),
                order.getId()).orElse(null);
        OrderInventorySnapshot inventorySnapshot = orderRepository.findInventorySnapshotByOrderId(order.getTenantId(),
                order.getId()).orElse(null);
        List<OrderItemDTO> itemDtos = orderRepository.findItemsByOrderId(order.getTenantId(), order.getId()).stream()
                .map(item -> new OrderItemDTO(item.getSkuId(), item.getSkuName(), item.getQuantity(),
                        item.getSalePrice(), item.getLineAmount()))
                .toList();
        return new OrderDetailDTO(order.getId(), order.getTenantId(), order.getOrderNo(), order.getUserId(),
                order.getOrderStatus(), order.getPayStatus(), order.getInventoryStatus(),
                paymentSnapshot == null ? order.getPaymentNo() : paymentSnapshot.paymentNo(),
                inventorySnapshot == null ? order.getReservationNo() : inventorySnapshot.reservationNo(),
                order.getCurrencyCode(), order.getTotalAmount(), order.getPayableAmount(),
                order.getCancelReason(), order.getCloseReason(), order.getCreatedAt(), order.getExpiredAt(), itemDtos,
                buildPaymentSnapshot(order, paymentSnapshot), buildInventorySnapshot(order, inventorySnapshot),
                order.getPaidAt(), order.getClosedAt());
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

    private String buildPaymentSnapshot(Order order, OrderPaymentSnapshot paymentSnapshot) {
        if (paymentSnapshot == null && order.getPaymentNo() == null) {
            return null;
        }
        String paymentNo = paymentSnapshot == null ? order.getPaymentNo() : paymentSnapshot.paymentNo();
        String payStatus = paymentSnapshot == null ? order.getPayStatus() : paymentSnapshot.payStatus();
        String channelCode = paymentSnapshot == null ? order.getPaymentChannelCode() : paymentSnapshot.channelCode();
        BigDecimal paidAmount = paymentSnapshot == null ? order.getPaidAmount() : paymentSnapshot.paidAmount();
        String channelStatus = paymentSnapshot == null
                ? order.getPaymentChannelStatus() : paymentSnapshot.channelStatus();
        String failureReason = paymentSnapshot == null
                ? order.getPaymentFailureReason() : paymentSnapshot.failureReason();
        return "paymentNo=" + paymentNo
                + ",payStatus=" + payStatus
                + ",channelCode=" + Objects.toString(channelCode, "N/A")
                + ",paidAmount=" + Objects.toString(paidAmount, "N/A")
                + ",channelStatus=" + Objects.toString(channelStatus, "N/A")
                + ",failureReason=" + Objects.toString(failureReason, "N/A");
    }

    private String buildInventorySnapshot(Order order, OrderInventorySnapshot inventorySnapshot) {
        String reservationNo = Objects.toString(
                inventorySnapshot == null ? order.getReservationNo() : inventorySnapshot.reservationNo(), "N/A");
        String inventoryStatus = inventorySnapshot == null ? order.getInventoryStatus() : inventorySnapshot.inventoryStatus();
        Long warehouseId = inventorySnapshot == null ? order.getWarehouseId() : inventorySnapshot.warehouseId();
        String failureReason = inventorySnapshot == null
                ? order.getInventoryFailureReason() : inventorySnapshot.failureReason();
        return "reservationNo=" + reservationNo
                + ",inventoryStatus=" + inventoryStatus
                + ",warehouseId=" + Objects.toString(warehouseId, "N/A")
                + ",failureReason=" + Objects.toString(failureReason, "N/A");
    }
}
