package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryOrderRepositoryImpl implements OrderRepository {

    private final Map<Long, Order> storage = new ConcurrentHashMap<>();
    private final Map<Long, Long> orderTenantStorage = new ConcurrentHashMap<>();
    private final Map<Long, List<OrderItem>> itemsStorage = new ConcurrentHashMap<>();
    private final Map<Long, Long> itemTenantStorage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1000L);

    @Override
    public Order insert(Order order) {
        Order savedOrder = withId(order, OrderId.of(idGenerator.getAndIncrement()));
        storage.put(toOrderIdValue(savedOrder), savedOrder);
        orderTenantStorage.put(toOrderIdValue(savedOrder), currentTenantId());
        return savedOrder;
    }

    @Override
    public Order update(Order order) {
        storage.put(toOrderIdValue(order), order);
        orderTenantStorage.put(toOrderIdValue(order), currentTenantId());
        return order;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        Long orderId = toOrderIdValue(id);
        if (!isTenantMatched(orderTenantStorage.get(orderId))) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(orderId));
    }

    @Override
    public Optional<Order> findByOrderNo(OrderNo orderNo) {
        return storage.values().stream()
                .filter(order -> isTenantMatched(orderTenantStorage.get(toOrderIdValue(order))))
                .filter(order -> toOrderNoValue(orderNo).equals(toOrderNoValue(order.getOrderNo())))
                .findFirst();
    }

    @Override
    public void updateItems(OrderId orderId, List<OrderItem> items) {
        Long orderIdValue = toOrderIdValue(orderId);
        itemsStorage.put(orderIdValue, items == null ? List.of() : List.copyOf(items));
        itemTenantStorage.put(orderIdValue, currentTenantId());
    }

    @Override
    public List<OrderItem> listItemsByOrderId(OrderId orderId) {
        Long orderIdValue = toOrderIdValue(orderId);
        if (!isTenantMatched(itemTenantStorage.get(orderIdValue))) {
            return List.of();
        }
        return itemsStorage.getOrDefault(orderIdValue, List.of());
    }

    @Override
    public List<Order> list() {
        return storage.values().stream().toList();
    }

    @Override
    public long count(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
        return filterOrders(userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                .size();
    }

    @Override
    public List<Order> page(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            int pageNo,
            int pageSize) {
        List<Order> filtered =
                filterOrders(userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo);
        int normalizedLimit = Math.max(pageSize, 1);
        int normalizedOffset = Math.max(0, (Math.max(pageNo, 1) - 1) * normalizedLimit);
        return filtered.stream().skip(normalizedOffset).limit(normalizedLimit).toList();
    }

    private List<Order> filterOrders(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
        List<Order> filtered = storage.values().stream()
                .filter(order -> isTenantMatched(orderTenantStorage.get(toOrderIdValue(order))))
                .filter(order -> userId == null || toUserIdValue(userId).equals(toUserIdValue(order)))
                .filter(order ->
                        orderNo == null || toOrderNoValue(order.getOrderNo()).contains(toOrderNoValue(orderNo)))
                .filter(order -> orderStatus == null || orderStatus == order.getOrderStatus())
                .filter(order -> payStatus == null || payStatus == order.getPayStatus())
                .filter(order -> inventoryStatus == null || inventoryStatus == order.getInventoryStatus())
                .filter(order -> createdAtFrom == null || !order.getCreatedAt().isBefore(createdAtFrom))
                .filter(order -> createdAtTo == null || !order.getCreatedAt().isAfter(createdAtTo))
                .sorted(Comparator.comparing(Order::getCreatedAt)
                        .reversed()
                        .thenComparing(this::toOrderIdValue, Comparator.reverseOrder()))
                .toList();
        return filtered;
    }

    private Long toOrderIdValue(Order order) {
        return order.getId() == null ? null : order.getId().value();
    }

    private Order withId(Order order, OrderId orderId) {
        return Order.reconstruct(
                orderId,
                order.getOrderNo(),
                order.getUserId(),
                order.getOrderStatus(),
                order.getPayStatus(),
                order.getInventoryStatus(),
                order.getPaymentNo(),
                order.getReservationNo(),
                order.getCurrencyCode(),
                order.getTotalAmount(),
                order.getPayableAmount(),
                order.getRemark(),
                order.getCancelReason(),
                order.getCloseReason(),
                order.getCreatedAt(),
                order.getExpiredAt(),
                order.getPaidAt(),
                order.getClosedAt(),
                order.getPaymentChannelCode(),
                order.getPaidAmount(),
                order.getPaymentChannelStatus(),
                order.getPaymentFailureReason(),
                order.getPaymentFailedAt(),
                order.getWarehouseCode(),
                order.getInventoryFailureReason(),
                order.getInventoryReleaseReason(),
                order.getInventoryReleasedAt(),
                order.getInventoryDeductedAt());
    }

    private Long toUserIdValue(Order order) {
        return order.getUserId() == null ? null : Long.valueOf(order.getUserId().value());
    }

    private Long toUserIdValue(UserId userId) {
        return userId == null ? null : Long.valueOf(userId.value());
    }

    private Long toOrderIdValue(OrderId orderId) {
        return orderId == null ? null : orderId.value();
    }

    private String toOrderNoValue(com.github.thundax.bacon.common.commerce.valueobject.OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }

    private Long currentTenantId() {
        return BaconContextHolder.requireTenantId();
    }

    private boolean isTenantMatched(Long tenantId) {
        return tenantId != null && tenantId.equals(currentTenantId());
    }
}
