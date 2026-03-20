package com.github.thundax.bacon.order.domain.service;

import com.github.thundax.bacon.order.domain.model.entity.Order;

public class OrderDomainService {

    public Order create(Long id, String orderNo, String customerName) {
        return new Order(id, orderNo, customerName);
    }
}
