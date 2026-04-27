package com.github.thundax.bacon.product.domain.model.enums;

import java.util.Arrays;

public enum CategoryStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static CategoryStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown category status: " + value));
    }
}
