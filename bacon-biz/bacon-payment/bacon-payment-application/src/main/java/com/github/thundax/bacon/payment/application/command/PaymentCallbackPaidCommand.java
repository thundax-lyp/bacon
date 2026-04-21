package com.github.thundax.bacon.payment.application.command;

public record PaymentCallbackPaidCommand(
        String channelCode, String paymentNo, String channelTransactionNo, String channelStatus, String rawPayload) {}
