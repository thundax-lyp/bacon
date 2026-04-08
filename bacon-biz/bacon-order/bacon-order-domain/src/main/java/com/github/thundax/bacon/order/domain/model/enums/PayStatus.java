package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 支付状态。
 */
public enum PayStatus {
    UNPAID,
    PAYING,
    PAID,
    FAILED,
    CLOSED;

    public String value() {
        return name();
    }

    public static PayStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported pay status: " + value));
    }
}
