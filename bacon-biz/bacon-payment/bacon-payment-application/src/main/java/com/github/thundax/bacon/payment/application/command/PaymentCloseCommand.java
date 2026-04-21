package com.github.thundax.bacon.payment.application.command;

public record PaymentCloseCommand(String paymentNo, String reason) {}
