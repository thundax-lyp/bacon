package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order insert(Order order);

    Order update(Order order);

    Optional<Order> findById(OrderId id);

    Optional<Order> findByOrderNo(OrderNo orderNo);

    void updateItems(OrderId orderId, List<OrderItem> items);

    List<OrderItem> listItemsByOrderId(OrderId orderId);

    void insertPayment(OrderPaymentSnapshot snapshot);

    void updatePayment(OrderPaymentSnapshot snapshot);

    Optional<OrderPaymentSnapshot> findPaymentByOrderId(OrderId orderId);

    void insertInventory(OrderInventorySnapshot snapshot);

    void updateInventory(OrderInventorySnapshot snapshot);

    Optional<OrderInventorySnapshot> findInventoryByOrderNo(OrderNo orderNo);

    void insertLog(OrderAuditLog auditLog);

    List<OrderAuditLog> listLogs(OrderNo orderNo);

    long count(
            UserId userId,
            OrderNo orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo);

    List<Order> page(
            UserId userId,
            OrderNo orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            int pageNo,
            int pageSize);

    List<Order> list();
}
