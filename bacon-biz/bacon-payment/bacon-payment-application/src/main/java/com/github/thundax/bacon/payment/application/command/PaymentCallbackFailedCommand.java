package com.github.thundax.bacon.payment.application.command;

public record PaymentCallbackFailedCommand(
        String channelCode, String paymentNo, String channelStatus, String rawPayload, String reason) {}
