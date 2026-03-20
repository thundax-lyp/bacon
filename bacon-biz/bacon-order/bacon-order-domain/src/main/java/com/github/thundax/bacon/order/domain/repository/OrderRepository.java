package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);
}
