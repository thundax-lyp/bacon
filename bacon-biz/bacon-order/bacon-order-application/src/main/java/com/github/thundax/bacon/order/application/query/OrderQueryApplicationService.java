package com.github.thundax.bacon.order.application.query;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.application.assembler.OrderDetailAssembler;
import com.github.thundax.bacon.order.application.assembler.OrderSummaryAssembler;
import com.github.thundax.bacon.order.application.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.application.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.result.OrderPageResult;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.repository.OrderInventorySnapshotRepository;
import com.github.thundax.bacon.order.domain.repository.OrderPaymentSnapshotRepository;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderQueryApplicationService {

    private final OrderRepository orderRepository;
    private final OrderInventorySnapshotRepository orderInventorySnapshotRepository;
    private final OrderPaymentSnapshotRepository orderPaymentSnapshotRepository;
    private final OrderDetailAssembler orderDetailAssembler;
    private final OrderSummaryAssembler orderSummaryAssembler;

    public OrderQueryApplicationService(
            OrderRepository orderRepository,
            OrderInventorySnapshotRepository orderInventorySnapshotRepository,
            OrderPaymentSnapshotRepository orderPaymentSnapshotRepository,
            OrderDetailAssembler orderDetailAssembler,
            OrderSummaryAssembler orderSummaryAssembler) {
        this.orderRepository = orderRepository;
        this.orderInventorySnapshotRepository = orderInventorySnapshotRepository;
        this.orderPaymentSnapshotRepository = orderPaymentSnapshotRepository;
        this.orderDetailAssembler = orderDetailAssembler;
        this.orderSummaryAssembler = orderSummaryAssembler;
    }

    public OrderDetailDTO getById(OrderId orderId) {
        BaconContextHolder.requireTenantId();
        return orderRepository
                .findById(orderId)
                .map(this::toDetail)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    public OrderDetailDTO getByOrderNo(OrderNo orderNo) {
        BaconContextHolder.requireTenantId();
        return orderRepository
                .findByOrderNo(orderNo)
                .map(this::toDetail)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNo));
    }

    public OrderPageResult page(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            Integer pageNo,
            Integer pageSize) {
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        long total = orderRepository.count(userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo);
        List<Order> page = total <= 0
                ? List.of()
                : orderRepository.page(
                        userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo, normalizedPageNo, normalizedPageSize);
        List<OrderSummaryDTO> records = page.stream().map(orderSummaryAssembler::toDto).toList();
        return new OrderPageResult(records, total, normalizedPageNo, normalizedPageSize);
    }

    private OrderDetailDTO toDetail(Order order) {
        OrderPaymentSnapshot paymentSnapshot =
                orderPaymentSnapshotRepository.findByOrderId(order.getId()).orElse(null);
        OrderInventorySnapshot inventorySnapshot = orderInventorySnapshotRepository
                .findByOrderNo(order.getOrderNo())
                .orElse(null);
        return orderDetailAssembler.toDto(
                order,
                orderRepository.listItemsByOrderId(order.getId()),
                paymentSnapshot,
                inventorySnapshot);
    }
}
