package com.github.thundax.bacon.order.application.service;

import org.springframework.stereotype.Service;

@Service
public class OrderTimeoutApplicationService {

    private final OrderApplicationService orderApplicationService;

    public OrderTimeoutApplicationService(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    public void closeExpiredOrder(Long tenantId, String orderNo, String reason) {
        orderApplicationService.closeExpiredOrder(tenantId, orderNo, reason);
    }
}
