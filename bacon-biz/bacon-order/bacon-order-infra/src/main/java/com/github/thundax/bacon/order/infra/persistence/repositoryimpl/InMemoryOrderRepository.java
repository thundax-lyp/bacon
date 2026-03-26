package com.github.thundax.bacon.order.infra.persistence.repositoryimpl;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageQuery;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageResult;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("test")
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> storage = new ConcurrentHashMap<>();
    private final Map<Long, List<OrderItem>> itemsStorage = new ConcurrentHashMap<>();
    private final Map<Long, OrderPaymentSnapshot> paymentSnapshotStorage = new ConcurrentHashMap<>();
    private final Map<Long, OrderInventorySnapshot> inventorySnapshotStorage = new ConcurrentHashMap<>();
    private final Map<String, List<OrderAuditLog>> auditLogStorage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1000L);

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(idGenerator.getAndIncrement());
        }
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Order> findByOrderNo(Long tenantId, String orderNo) {
        return storage.values().stream()
                .filter(order -> tenantId.equals(order.getTenantId()))
                .filter(order -> orderNo.equals(order.getOrderNo()))
                .findFirst();
    }

    @Override
    public void saveItems(Long tenantId, Long orderId, List<OrderItem> items) {
        itemsStorage.put(orderId, items == null ? List.of() : List.copyOf(items));
    }

    @Override
    public List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId) {
        return itemsStorage.getOrDefault(orderId, List.of()).stream()
                .filter(item -> tenantId.equals(item.getTenantId()))
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
        inventorySnapshotStorage.put(snapshot.orderId(), snapshot);
    }

    @Override
    public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderId(Long tenantId, Long orderId) {
        OrderInventorySnapshot snapshot = inventorySnapshotStorage.get(orderId);
        if (snapshot == null || !tenantId.equals(snapshot.tenantId())) {
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    @Override
    public void saveAuditLog(OrderAuditLog auditLog) {
        String key = auditLog.tenantId() + ":" + auditLog.orderNo();
        auditLogStorage.computeIfAbsent(key, unused -> new java.util.ArrayList<>()).add(auditLog);
    }

    @Override
    public List<OrderAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return List.copyOf(auditLogStorage.getOrDefault(tenantId + ":" + orderNo, List.of()));
    }

    @Override
    public OrderPageResult pageOrders(OrderPageQuery query) {
        List<Order> filtered = storage.values().stream()
                .filter(order -> query.tenantId() == null || query.tenantId().equals(order.getTenantId()))
                .filter(order -> query.userId() == null || query.userId().equals(order.getUserId()))
                .filter(order -> query.orderNo() == null || order.getOrderNo().contains(query.orderNo()))
                .filter(order -> query.orderStatus() == null || query.orderStatus().equals(order.getOrderStatus()))
                .filter(order -> query.payStatus() == null || query.payStatus().equals(order.getPayStatus()))
                .filter(order -> query.inventoryStatus() == null || query.inventoryStatus().equals(order.getInventoryStatus()))
                .filter(order -> query.createdAtFrom() == null || !order.getCreatedAt().isBefore(query.createdAtFrom()))
                .filter(order -> query.createdAtTo() == null || !order.getCreatedAt().isAfter(query.createdAtTo()))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed().thenComparing(Order::getId).reversed())
                .toList();
        long total = filtered.size();
        int offset = Math.max(query.offset(), 0);
        int limit = Math.max(query.limit(), 1);
        List<Order> records = filtered.stream()
                .skip(offset)
                .limit(limit)
                .toList();
        return new OrderPageResult(records, total);
    }

    @Override
    public List<Order> findAll() {
        return storage.values().stream().toList();
    }
}
