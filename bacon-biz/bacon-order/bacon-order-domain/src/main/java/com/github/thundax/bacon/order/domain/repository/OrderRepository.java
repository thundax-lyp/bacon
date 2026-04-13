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

    Optional<Order> findByOrderNo(String orderNo);

    void saveItems(Long orderId, List<OrderItem> items);

    List<OrderItem> findItemsByOrderId(Long orderId, String currencyCode);

    void savePaymentSnapshot(OrderPaymentSnapshot snapshot);

    Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long orderId, String currencyCode);

    void saveInventorySnapshot(OrderInventorySnapshot snapshot);

    Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(String orderNo);

    void saveAuditLog(OrderAuditLog auditLog);

    List<OrderAuditLog> findAuditLogs(String orderNo);

    long countOrders(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo);

    List<Order> pageOrders(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            int offset,
            int limit);

    List<Order> findAll();
}
