package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.order.application.executor.OrderIdempotencyExecutor;
import org.springframework.stereotype.Service;

@Service
public class OrderCancelApplicationService {

    private final OrderApplicationService orderApplicationService;
    private final OrderIdempotencyExecutor orderIdempotencyExecutor;

    public OrderCancelApplicationService(OrderApplicationService orderApplicationService,
                                         OrderIdempotencyExecutor orderIdempotencyExecutor) {
        this.orderApplicationService = orderApplicationService;
        this.orderIdempotencyExecutor = orderIdempotencyExecutor;
    }

    public void cancel(Long tenantId, String orderNo, String reason) {
        String resolvedReason = reason == null || reason.isBlank() ? "USER_CANCELLED" : reason;
        orderIdempotencyExecutor.execute(OrderIdempotencyExecutor.EVENT_CANCEL, tenantId, orderNo, null,
                () -> orderApplicationService.cancelOrder(tenantId, orderNo, resolvedReason));
    }
}
