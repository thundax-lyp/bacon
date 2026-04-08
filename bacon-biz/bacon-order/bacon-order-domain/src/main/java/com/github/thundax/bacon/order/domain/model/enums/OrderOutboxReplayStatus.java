package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 订单出站死信回放状态。
 */
public enum OrderOutboxReplayStatus {
    PENDING,
    SUCCESS,
    FAILED;

    public String value() {
        return name();
    }

    public static OrderOutboxReplayStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported order outbox replay status: " + value));
    }
}
