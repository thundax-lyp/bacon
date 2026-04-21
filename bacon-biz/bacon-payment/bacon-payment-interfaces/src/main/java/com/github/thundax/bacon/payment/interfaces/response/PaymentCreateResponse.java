package com.github.thundax.bacon.payment.interfaces.response;

import java.time.Instant;

public record PaymentCreateResponse(
        String paymentNo,
        String orderNo,
        String channelCode,
        String paymentStatus,
        String payPayload,
        Instant expiredAt,
        String failureReason) {}
