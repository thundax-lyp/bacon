package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNo(Long tenantId, String orderNo);

    void saveItems(Long tenantId, Long orderId, List<OrderItem> items);

    List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId);

    List<Order> findAll();
}
