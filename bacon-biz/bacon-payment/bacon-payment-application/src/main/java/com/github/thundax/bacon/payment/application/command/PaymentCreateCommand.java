package com.github.thundax.bacon.payment.application.command;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCreateCommand(
        String orderNo,
        Long userId,
        BigDecimal amount,
        String channelCode,
        String subject,
        Instant expiredAt) {}
