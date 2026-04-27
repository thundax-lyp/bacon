package com.github.thundax.bacon.product.domain.model.enums;

import java.util.Arrays;

public enum SkuStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static SkuStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown sku status: " + value));
    }
}
