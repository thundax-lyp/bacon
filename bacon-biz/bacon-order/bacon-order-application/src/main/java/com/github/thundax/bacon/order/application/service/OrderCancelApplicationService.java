package com.github.thundax.bacon.order.application.service;

import org.springframework.stereotype.Service;

@Service
public class OrderCancelApplicationService {

    private final OrderApplicationService orderApplicationService;

    public OrderCancelApplicationService(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    public void cancel(Long tenantId, String orderNo) {
        orderApplicationService.cancelOrder(tenantId, orderNo, "USER_CANCELLED");
    }
}
