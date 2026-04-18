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

    Order insertOrder(Order order);

    Order updateOrder(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNo(String orderNo);

    void updateItems(Long orderId, List<OrderItem> items);

    List<OrderItem> findItemsByOrderId(Long orderId);

    void insertPaymentSnapshot(OrderPaymentSnapshot snapshot);

    void updatePaymentSnapshot(OrderPaymentSnapshot snapshot);

    Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long orderId);

    void insertInventorySnapshot(OrderInventorySnapshot snapshot);

    void updateInventorySnapshot(OrderInventorySnapshot snapshot);

    Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(String orderNo);

    void insertAuditLog(OrderAuditLog auditLog);

    List<OrderAuditLog> findAuditLogs(String orderNo);

    long count(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo);

    List<Order> page(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            int pageNo,
            int pageSize);

    List<Order> findAll();
}
