package com.github.thundax.bacon.product.domain.model.enums;

import java.util.Arrays;

public enum IdempotencyStatus {
    PROCESSING,
    SUCCESS,
    FAILED;

    public String value() {
        return name();
    }

    public static IdempotencyStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown idempotency status: " + value));
    }
}
