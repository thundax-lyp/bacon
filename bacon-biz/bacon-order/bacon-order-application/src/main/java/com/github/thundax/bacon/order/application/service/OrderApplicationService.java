package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderItemDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
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

@Service
public class OrderApplicationService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 200;

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService = new OrderDomainService();
    private final OrderNoGenerator orderNoGenerator;

    public OrderApplicationService(OrderRepository orderRepository, OrderNoGenerator orderNoGenerator) {
        this.orderRepository = orderRepository;
        this.orderNoGenerator = orderNoGenerator;
    }

    public OrderSummaryDTO create(CreateOrderCommand command) {
        long generatedId = System.currentTimeMillis();
        String orderNo = orderNoGenerator.nextOrderNo();
        Order order = new Order(generatedId, 1001L, orderNo, 2001L, command.customerName());
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
        int pageNo = normalizePageNo(query.getPageNo());
        int pageSize = normalizePageSize(query.getPageSize());
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
                List.of(new OrderItemDTO(101L, order.getCustomerName() + "-item", 1, BigDecimal.TEN, BigDecimal.TEN)),
                "mock-payment", "mock-inventory", order.getPaidAt(), order.getClosedAt());
    }

    private int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? DEFAULT_PAGE_NO : pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
