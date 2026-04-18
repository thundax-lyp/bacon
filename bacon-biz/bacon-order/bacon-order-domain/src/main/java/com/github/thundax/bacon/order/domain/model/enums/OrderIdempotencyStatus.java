package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 订单幂等处理状态。
 */
public enum OrderIdempotencyStatus {
    READY,
    PROCESSING,
    SUCCESS,
    FAILED;

    public String value() {
        return name();
    }

    public static OrderIdempotencyStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown order idempotency status: " + value));
    }
}
