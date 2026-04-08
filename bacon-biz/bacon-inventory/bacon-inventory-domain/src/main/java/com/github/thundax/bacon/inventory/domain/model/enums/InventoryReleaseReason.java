package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryReleaseReason {
    USER_CANCELLED,
    SYSTEM_CANCELLED,
    PAYMENT_CREATE_FAILED,
    PAYMENT_FAILED,
    TIMEOUT_CLOSED;

    public String value() {
        return name();
    }

    public static InventoryReleaseReason from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory release reason: " + value));
    }
}
