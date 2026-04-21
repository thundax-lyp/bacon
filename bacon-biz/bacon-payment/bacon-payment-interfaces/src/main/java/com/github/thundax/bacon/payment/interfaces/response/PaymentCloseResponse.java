package com.github.thundax.bacon.payment.interfaces.response;

public record PaymentCloseResponse(
        String paymentNo,
        String orderNo,
        String paymentStatus,
        String closeResult,
        String closeReason,
        String failureReason) {}
