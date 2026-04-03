package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNo(Long tenantId, String orderNo);

    void saveItems(Long tenantId, Long orderId, List<OrderItem> items);

    List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId);

    void savePaymentSnapshot(OrderPaymentSnapshot snapshot);

    Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long tenantId, Long orderId);

    void saveInventorySnapshot(OrderInventorySnapshot snapshot);

    Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(Long tenantId, String orderNo);

    void saveAuditLog(OrderAuditLog auditLog);

    List<OrderAuditLog> findAuditLogs(Long tenantId, String orderNo);

    long countOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                     String inventoryStatus, Instant createdAtFrom, Instant createdAtTo);

    List<Order> pageOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                           String inventoryStatus, Instant createdAtFrom, Instant createdAtTo,
                           int offset, int limit);

    List<Order> findAll();
}
