package com.github.thundax.bacon.payment.interfaces.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDetailResponse(Long tenantId, String paymentNo, String orderNo, Long userId, String channelCode,
                                    String paymentStatus, BigDecimal amount, BigDecimal paidAmount,
                                    Instant createdAt, Instant expiredAt, Instant paidAt, String subject,
                                    Instant closedAt, String channelTransactionNo, String channelStatus,
                                    String callbackSummary) {
}
