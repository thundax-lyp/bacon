package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.order.application.executor.OrderIdempotencyExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class OrderPaymentResultApplicationService {

    private final OrderApplicationService orderApplicationService;
    private final OrderIdempotencyExecutor orderIdempotencyExecutor;

    public OrderPaymentResultApplicationService(OrderApplicationService orderApplicationService,
                                                OrderIdempotencyExecutor orderIdempotencyExecutor) {
        this.orderApplicationService = orderApplicationService;
        this.orderIdempotencyExecutor = orderIdempotencyExecutor;
    }

    public void markPaid(Long tenantId, String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime) {
        orderIdempotencyExecutor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID, tenantId, orderNo, paymentNo,
                () -> orderApplicationService.markPaid(tenantId, orderNo, paymentNo, channelCode, paidAmount, paidTime));
    }

    public void markPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason, String channelStatus,
                                  Instant failedTime) {
        orderIdempotencyExecutor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAYMENT_FAILED, tenantId, orderNo,
                paymentNo, () -> orderApplicationService.markPaymentFailed(tenantId, orderNo, paymentNo, reason,
                        channelStatus, failedTime));
    }
}
