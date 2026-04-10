package com.github.thundax.bacon.order.api.facade;

import java.math.BigDecimal;
import java.time.Instant;

public interface OrderCommandFacade {

    void markPaid(String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime);

    void markPaymentFailed(String orderNo, String paymentNo, String reason, String channelStatus, Instant failedTime);

    void closeExpiredOrder(String orderNo, String reason);
}
