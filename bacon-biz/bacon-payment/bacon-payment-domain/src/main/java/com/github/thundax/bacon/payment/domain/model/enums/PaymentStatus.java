package com.github.thundax.bacon.payment.domain.model.enums;

import java.util.Arrays;

/**
 * 支付状态。
 */
public enum PaymentStatus {
    CREATED,
    PAYING,
    PAID,
    FAILED,
    CLOSED;

    public String value() {
        return name();
    }

    public static PaymentStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment status: " + value));
    }
}
