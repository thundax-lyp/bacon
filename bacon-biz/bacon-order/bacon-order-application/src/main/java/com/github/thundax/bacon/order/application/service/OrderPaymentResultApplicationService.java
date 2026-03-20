package com.github.thundax.bacon.order.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class OrderPaymentResultApplicationService {

    private final OrderApplicationService orderApplicationService;

    public OrderPaymentResultApplicationService(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    public void markPaid(Long tenantId, String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime) {
        orderApplicationService.markPaid(tenantId, orderNo, paymentNo, paidTime);
    }

    public void markPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason, String channelStatus,
                                  Instant failedTime) {
        orderApplicationService.markPaymentFailed(tenantId, orderNo, paymentNo, reason);
    }
}
