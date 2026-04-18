package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
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

    List<Order> list();

    long count(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo);

    List<Order> page(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            int pageNo,
            int pageSize);
}
