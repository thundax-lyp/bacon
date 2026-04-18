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

    Order insert(Order order);

    Order update(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNo(String orderNo);

    void updateItems(Long orderId, List<OrderItem> items);

    List<OrderItem> listItemsByOrderId(Long orderId);

    void insertPayment(OrderPaymentSnapshot snapshot);

    void updatePayment(OrderPaymentSnapshot snapshot);

    Optional<OrderPaymentSnapshot> findPaymentByOrderId(Long orderId);

    void insertInventory(OrderInventorySnapshot snapshot);

    void updateInventory(OrderInventorySnapshot snapshot);

    Optional<OrderInventorySnapshot> findInventoryByOrderNo(String orderNo);

    void insertLog(OrderAuditLog auditLog);

    List<OrderAuditLog> listLogs(String orderNo);

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

    List<Order> list();
}
