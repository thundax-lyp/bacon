package com.github.thundax.bacon.product.domain.model.enums;

import java.util.Arrays;

public enum ProductStatus {
    DRAFT,
    ON_SALE,
    OFF_SALE,
    ARCHIVED;

    public String value() {
        return name();
    }

    public static ProductStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown product status: " + value));
    }
}
