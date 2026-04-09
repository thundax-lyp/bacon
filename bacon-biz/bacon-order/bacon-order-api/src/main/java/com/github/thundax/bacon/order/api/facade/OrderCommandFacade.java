package com.github.thundax.bacon.order.api.facade;

import java.math.BigDecimal;
import java.time.Instant;

public interface OrderCommandFacade {

    void markPaid(
            Long tenantId,
            String orderNo,
            String paymentNo,
            String channelCode,
            BigDecimal paidAmount,
            Instant paidTime);

    void markPaymentFailed(
            Long tenantId, String orderNo, String paymentNo, String reason, String channelStatus, Instant failedTime);

    void closeExpiredOrder(Long tenantId, String orderNo, String reason);
}
