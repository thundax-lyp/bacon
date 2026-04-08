package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 订单出站事件状态。
 */
public enum OrderOutboxStatus {
    NEW,
    RETRYING,
    PROCESSING,
    DEAD;

    public String value() {
        return name();
    }

    public static OrderOutboxStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported order outbox status: " + value));
    }
}
