package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.order.application.executor.OrderIdempotencyExecutor;
import org.springframework.stereotype.Service;

@Service
public class OrderTimeoutApplicationService {

    private final OrderApplicationService orderApplicationService;
    private final OrderIdempotencyExecutor orderIdempotencyExecutor;

    public OrderTimeoutApplicationService(OrderApplicationService orderApplicationService,
                                          OrderIdempotencyExecutor orderIdempotencyExecutor) {
        this.orderApplicationService = orderApplicationService;
        this.orderIdempotencyExecutor = orderIdempotencyExecutor;
    }

    public void closeExpiredOrder(Long tenantId, String orderNo, String reason) {
        orderIdempotencyExecutor.execute(OrderIdempotencyExecutor.EVENT_CLOSE_EXPIRED, tenantId, orderNo, null,
                () -> orderApplicationService.closeExpiredOrder(tenantId, orderNo, reason));
    }
}
