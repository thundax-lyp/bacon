package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 支付渠道状态。
 */
public enum PaymentChannelStatus {
    PAYING,
    SUCCESS,
    FAILED,
    CLOSED;

    public String value() {
        return name();
    }

    public static PaymentChannelStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment channel status: " + value));
    }
}
