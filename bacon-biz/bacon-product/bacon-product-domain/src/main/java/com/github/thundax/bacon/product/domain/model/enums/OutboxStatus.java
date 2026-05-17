package com.github.thundax.bacon.product.domain.model.enums;

import java.util.Arrays;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    DEAD;

    public String value() {
        return name();
    }

    public static OutboxStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown outbox status: " + value));
    }
}
