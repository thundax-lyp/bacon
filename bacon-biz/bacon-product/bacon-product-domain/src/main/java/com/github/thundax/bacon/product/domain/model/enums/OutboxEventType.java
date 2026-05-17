package com.github.thundax.bacon.product.domain.model.enums;

import java.util.Arrays;

public enum OutboxEventType {
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_STATUS_CHANGED,
    PRODUCT_ARCHIVED;

    public String value() {
        return name();
    }

    public static OutboxEventType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown outbox event type: " + value));
    }
}
