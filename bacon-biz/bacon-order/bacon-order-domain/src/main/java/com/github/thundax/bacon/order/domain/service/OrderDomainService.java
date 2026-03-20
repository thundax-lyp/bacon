package com.github.thundax.bacon.order.domain.service;

import com.github.thundax.bacon.order.domain.model.entity.Order;

public class OrderDomainService {

    public Order create(Long id, Long tenantId, String orderNo, Long userId, String customerName) {
        return new Order(id, tenantId, orderNo, userId, customerName);
    }
}
