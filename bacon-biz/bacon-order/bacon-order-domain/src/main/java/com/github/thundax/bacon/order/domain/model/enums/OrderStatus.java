package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 订单主状态。
 */
public enum OrderStatus {
    CREATED,
    RESERVING_STOCK,
    PENDING_PAYMENT,
    PAID,
    CANCELLED,
    CLOSED;

    public String value() {
        return name();
    }

    public static OrderStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported order status: " + value));
    }
}
