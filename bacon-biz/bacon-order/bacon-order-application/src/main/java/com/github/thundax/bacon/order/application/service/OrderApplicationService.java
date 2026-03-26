package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderItemDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.command.CreateOrderItemCommand;
import com.github.thundax.bacon.order.application.command.CreateOrderCommand;
import com.github.thundax.bacon.order.application.query.GetOrderQuery;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderDomainService;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class OrderApplicationService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService = new OrderDomainService();
    private final OrderNoGenerator orderNoGenerator;

    public OrderApplicationService(OrderRepository orderRepository, OrderNoGenerator orderNoGenerator) {
        this.orderRepository = orderRepository;
        this.orderNoGenerator = orderNoGenerator;
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
        return toSummary(orderRepository.save(order));
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
        List<OrderSummaryDTO> records = orderRepository.findAll().stream()
                .filter(order -> query.getTenantId() == null || query.getTenantId().equals(order.getTenantId()))
                .filter(order -> query.getUserId() == null || query.getUserId().equals(order.getUserId()))
                .filter(order -> query.getOrderNo() == null || order.getOrderNo().contains(query.getOrderNo()))
                .map(this::toSummary)
                .toList();
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize(), DEFAULT_PAGE_SIZE);
        return new OrderPageResultDTO(records, records.size(), pageNo, pageSize);
    }

    public void cancelOrder(Long tenantId, String orderNo, String reason) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        order.cancel(reason);
    }

    public void markPaid(Long tenantId, String orderNo, String paymentNo, Instant paidTime) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        order.markPaid(paymentNo, paidTime);
    }

    public void markPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        order.markPaymentFailed(paymentNo, reason);
    }

    public void closeExpiredOrder(Long tenantId, String orderNo, String reason) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        order.closeExpired(reason);
    }

    private OrderSummaryDTO toSummary(Order order) {
        return new OrderSummaryDTO(order.getId(), order.getTenantId(), order.getOrderNo(), order.getUserId(),
                order.getOrderStatus(), order.getPayStatus(), order.getInventoryStatus(), order.getPaymentNo(),
                order.getReservationNo(), order.getCurrencyCode(), order.getTotalAmount(), order.getPayableAmount(),
                order.getCancelReason(), order.getCloseReason(), order.getCreatedAt(), order.getExpiredAt());
    }

    private OrderDetailDTO toDetail(Order order) {
        return new OrderDetailDTO(order.getId(), order.getTenantId(), order.getOrderNo(), order.getUserId(),
                order.getOrderStatus(), order.getPayStatus(), order.getInventoryStatus(), order.getPaymentNo(),
                order.getReservationNo(), order.getCurrencyCode(), order.getTotalAmount(), order.getPayableAmount(),
                order.getCancelReason(), order.getCloseReason(), order.getCreatedAt(), order.getExpiredAt(),
                List.of(new OrderItemDTO(101L, "demo-item", 1, BigDecimal.TEN, BigDecimal.TEN)),
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
        return "paymentNo=" + order.getPaymentNo() + ",payStatus=" + order.getPayStatus();
    }

    private String buildInventorySnapshot(Order order) {
        String reservationNo = Objects.toString(order.getReservationNo(), "N/A");
        return "reservationNo=" + reservationNo + ",inventoryStatus=" + order.getInventoryStatus();
    }
}
