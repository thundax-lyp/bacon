package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;
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
    private final Map<Long, List<OrderItem>> itemsStorage = new ConcurrentHashMap<>();
    private final Map<Long, OrderPaymentSnapshot> paymentSnapshotStorage = new ConcurrentHashMap<>();
    private final Map<String, OrderInventorySnapshot> inventorySnapshotStorage = new ConcurrentHashMap<>();
    private final Map<String, List<OrderAuditLog>> auditLogStorage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1000L);

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(OrderId.of(String.valueOf(idGenerator.getAndIncrement())));
        }
        storage.put(toOrderIdValue(order), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Order> findByOrderNo(Long tenantId, String orderNo) {
        return storage.values().stream()
                .filter(order -> tenantId.equals(order.getTenantIdValue()))
                .filter(order -> orderNo.equals(order.getOrderNoValue()))
                .findFirst();
    }

    @Override
    public void saveItems(Long tenantId, Long orderId, List<OrderItem> items) {
        itemsStorage.put(orderId, items == null ? List.of() : List.copyOf(items));
    }

    @Override
    public List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId, String currencyCode) {
        return itemsStorage.getOrDefault(orderId, List.of()).stream()
                .filter(item -> tenantId.equals(item.getTenantIdValue()))
                .toList();
    }

    @Override
    public void savePaymentSnapshot(OrderPaymentSnapshot snapshot) {
        paymentSnapshotStorage.put(snapshot.orderId(), snapshot);
    }

    @Override
    public Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long tenantId, Long orderId) {
        OrderPaymentSnapshot snapshot = paymentSnapshotStorage.get(orderId);
        if (snapshot == null || !tenantId.equals(snapshot.tenantId())) {
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    @Override
    public void saveInventorySnapshot(OrderInventorySnapshot snapshot) {
        inventorySnapshotStorage.put(snapshot.orderNoValue(), snapshot);
    }

    @Override
    public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(Long tenantId, String orderNo) {
        OrderInventorySnapshot snapshot = inventorySnapshotStorage.get(orderNo);
        if (snapshot == null || !tenantId.equals(snapshot.tenantIdValue())) {
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    @Override
    public void saveAuditLog(OrderAuditLog auditLog) {
        String key = toTenantIdValue(auditLog.tenantId()) + ":" + auditLog.orderNo();
        auditLogStorage.computeIfAbsent(key, unused -> new java.util.ArrayList<>()).add(auditLog);
    }

    @Override
    public List<OrderAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return List.copyOf(auditLogStorage.getOrDefault(tenantId + ":" + orderNo, List.of()));
    }

    @Override
    public long countOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                            String inventoryStatus, Instant createdAtFrom, Instant createdAtTo) {
        return filterOrders(tenantId, userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                .size();
    }

    @Override
    public List<Order> pageOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                                  String inventoryStatus, Instant createdAtFrom, Instant createdAtTo,
                                  int offset, int limit) {
        List<Order> filtered = filterOrders(tenantId, userId, orderNo, orderStatus, payStatus, inventoryStatus,
                createdAtFrom, createdAtTo);
        int normalizedOffset = Math.max(offset, 0);
        int normalizedLimit = Math.max(limit, 1);
        return filtered.stream()
                .skip(normalizedOffset)
                .limit(normalizedLimit)
                .toList();
    }

    private List<Order> filterOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                                     String inventoryStatus, Instant createdAtFrom, Instant createdAtTo) {
        List<Order> filtered = storage.values().stream()
                .filter(order -> tenantId == null || tenantId.equals(order.getTenantIdValue()))
                .filter(order -> userId == null || userId.equals(toUserIdValue(order)))
                .filter(order -> orderNo == null || order.getOrderNoValue().contains(orderNo))
                .filter(order -> orderStatus == null || orderStatus.equals(order.getOrderStatus()))
                .filter(order -> payStatus == null || payStatus.equals(order.getPayStatus()))
                .filter(order -> inventoryStatus == null || inventoryStatus.equals(order.getInventoryStatus()))
                .filter(order -> createdAtFrom == null || !order.getCreatedAt().isBefore(createdAtFrom))
                .filter(order -> createdAtTo == null || !order.getCreatedAt().isAfter(createdAtTo))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed()
                        .thenComparing(this::toOrderIdValue, Comparator.reverseOrder()))
                .toList();
        return filtered;
    }

    @Override
    public List<Order> findAll() {
        return storage.values().stream().toList();
    }

    private Long toOrderIdValue(Order order) {
        return order.getId() == null ? null : Long.valueOf(order.getId().value());
    }

    private Long toUserIdValue(Order order) {
        return order.getUserId() == null ? null : Long.valueOf(order.getUserId().value());
    }

    private Long toTenantIdValue(TenantId tenantId) {
        return tenantId == null ? null : Long.valueOf(tenantId.value());
    }
}
