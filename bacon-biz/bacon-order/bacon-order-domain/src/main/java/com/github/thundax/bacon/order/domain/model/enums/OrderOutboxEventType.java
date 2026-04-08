package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 订单出站事件类型。
 */
public enum OrderOutboxEventType {
    RESERVE_STOCK,
    CREATE_PAYMENT,
    RELEASE_STOCK;

    public String value() {
        return name();
    }

    public static OrderOutboxEventType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported order outbox event type: " + value));
    }
}
