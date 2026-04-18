package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
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
    private final Map<Long, OrderPaymentSnapshot> paymentSnapshotStorage = new ConcurrentHashMap<>();
    private final Map<Long, Long> paymentSnapshotTenantStorage = new ConcurrentHashMap<>();
    private final Map<String, OrderInventorySnapshot> inventorySnapshotStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> inventorySnapshotTenantStorage = new ConcurrentHashMap<>();
    private final Map<String, List<OrderAuditLog>> auditLogStorage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1000L);

    @Override
    public Order insertOrder(Order order) {
        order.setId(OrderId.of(idGenerator.getAndIncrement()));
        storage.put(toOrderIdValue(order), order);
        orderTenantStorage.put(toOrderIdValue(order), currentTenantId());
        return order;
    }

    @Override
    public Order updateOrder(Order order) {
        storage.put(toOrderIdValue(order), order);
        orderTenantStorage.put(toOrderIdValue(order), currentTenantId());
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        if (!isTenantMatched(orderTenantStorage.get(id))) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return storage.values().stream()
                .filter(order -> isTenantMatched(orderTenantStorage.get(toOrderIdValue(order))))
                .filter(order -> orderNo.equals(toOrderNoValue(order.getOrderNo())))
                .findFirst();
    }

    @Override
    public void updateItems(Long orderId, List<OrderItem> items) {
        itemsStorage.put(orderId, items == null ? List.of() : List.copyOf(items));
        itemTenantStorage.put(orderId, currentTenantId());
    }

    @Override
    public List<OrderItem> findItemsByOrderId(Long orderId) {
        if (!isTenantMatched(itemTenantStorage.get(orderId))) {
            return List.of();
        }
        return itemsStorage.getOrDefault(orderId, List.of());
    }

    @Override
    public void insertPaymentSnapshot(OrderPaymentSnapshot snapshot) {
        Long orderId = toOrderIdValue(snapshot.getOrderId());
        paymentSnapshotStorage.put(orderId, snapshot);
        paymentSnapshotTenantStorage.put(orderId, currentTenantId());
    }

    @Override
    public void updatePaymentSnapshot(OrderPaymentSnapshot snapshot) {
        Long orderId = toOrderIdValue(snapshot.getOrderId());
        paymentSnapshotStorage.put(orderId, snapshot);
        paymentSnapshotTenantStorage.put(orderId, currentTenantId());
    }

    @Override
    public Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long orderId) {
        OrderPaymentSnapshot snapshot = paymentSnapshotStorage.get(orderId);
        if (snapshot == null || !isTenantMatched(paymentSnapshotTenantStorage.get(orderId))) {
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    @Override
    public void insertInventorySnapshot(OrderInventorySnapshot snapshot) {
        String orderNo = toOrderNoValue(snapshot.getOrderNo());
        inventorySnapshotStorage.put(orderNo, snapshot);
        inventorySnapshotTenantStorage.put(orderNo, currentTenantId());
    }

    @Override
    public void updateInventorySnapshot(OrderInventorySnapshot snapshot) {
        String orderNo = toOrderNoValue(snapshot.getOrderNo());
        inventorySnapshotStorage.put(orderNo, snapshot);
        inventorySnapshotTenantStorage.put(orderNo, currentTenantId());
    }

    @Override
    public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(String orderNo) {
        OrderInventorySnapshot snapshot = inventorySnapshotStorage.get(orderNo);
        if (snapshot == null || !isTenantMatched(inventorySnapshotTenantStorage.get(orderNo))) {
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    @Override
    public void insertAuditLog(OrderAuditLog auditLog) {
        String key = currentTenantId() + ":" + toOrderNoValue(auditLog.getOrderNo());
        auditLogStorage
                .computeIfAbsent(key, unused -> new java.util.ArrayList<>())
                .add(auditLog);
    }

    @Override
    public List<OrderAuditLog> findAuditLogs(String orderNo) {
        return List.copyOf(auditLogStorage.getOrDefault(currentTenantId() + ":" + orderNo, List.of()));
    }

    @Override
    public long count(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
        return filterOrders(userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                .size();
    }

    @Override
    public List<Order> page(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
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
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
        List<Order> filtered = storage.values().stream()
                .filter(order -> isTenantMatched(orderTenantStorage.get(toOrderIdValue(order))))
                .filter(order -> userId == null || userId.equals(toUserIdValue(order)))
                .filter(order ->
                        orderNo == null || toOrderNoValue(order.getOrderNo()).contains(orderNo))
                .filter(order -> orderStatus == null || orderStatus.equals(toOrderStatusValue(order.getOrderStatus())))
                .filter(order -> payStatus == null || payStatus.equals(toPayStatusValue(order.getPayStatus())))
                .filter(order -> inventoryStatus == null
                        || inventoryStatus.equals(toInventoryStatusValue(order.getInventoryStatus())))
                .filter(order -> createdAtFrom == null || !order.getCreatedAt().isBefore(createdAtFrom))
                .filter(order -> createdAtTo == null || !order.getCreatedAt().isAfter(createdAtTo))
                .sorted(Comparator.comparing(Order::getCreatedAt)
                        .reversed()
                        .thenComparing(this::toOrderIdValue, Comparator.reverseOrder()))
                .toList();
        return filtered;
    }

    @Override
    public List<Order> findAll() {
        return storage.values().stream().toList();
    }

    private Long toOrderIdValue(Order order) {
        return order.getId() == null ? null : order.getId().value();
    }

    private Long toUserIdValue(Order order) {
        return order.getUserId() == null ? null : Long.valueOf(order.getUserId().value());
    }

    private Long toOrderIdValue(OrderId orderId) {
        return orderId == null ? null : orderId.value();
    }

    private String toOrderNoValue(com.github.thundax.bacon.common.commerce.valueobject.OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }

    private String toOrderStatusValue(com.github.thundax.bacon.order.domain.model.enums.OrderStatus orderStatus) {
        return orderStatus == null ? null : orderStatus.value();
    }

    private String toPayStatusValue(com.github.thundax.bacon.order.domain.model.enums.PayStatus payStatus) {
        return payStatus == null ? null : payStatus.value();
    }

    private String toInventoryStatusValue(
            com.github.thundax.bacon.order.domain.model.enums.InventoryStatus inventoryStatus) {
        return inventoryStatus == null ? null : inventoryStatus.value();
    }

    private Long currentTenantId() {
        return BaconContextHolder.requireTenantId();
    }

    private boolean isTenantMatched(Long tenantId) {
        return tenantId != null && tenantId.equals(currentTenantId());
    }
}
