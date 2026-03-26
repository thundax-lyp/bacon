package com.github.thundax.bacon.order.domain.model.entity;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderPaymentSnapshot(
        Long id,
        Long tenantId,
        Long orderId,
        String paymentNo,
        String channelCode,
        String payStatus,
        BigDecimal paidAmount,
        Instant paidTime,
        String failureReason,
        String channelStatus,
        Instant updatedAt
) {
}
