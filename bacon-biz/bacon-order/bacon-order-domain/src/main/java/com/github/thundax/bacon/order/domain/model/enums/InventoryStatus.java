package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 库存状态。
 */
public enum InventoryStatus {
    UNRESERVED,
    RESERVING,
    RESERVED,
    RELEASED,
    DEDUCTED,
    FAILED;

    public String value() {
        return name();
    }

    public static InventoryStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported inventory status: " + value));
    }
}
