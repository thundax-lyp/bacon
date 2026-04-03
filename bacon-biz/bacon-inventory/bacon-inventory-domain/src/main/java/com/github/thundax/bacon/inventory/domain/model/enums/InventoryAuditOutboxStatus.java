package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryAuditOutboxStatus {

    NEW("NEW"),
    RETRYING("RETRYING"),
    PROCESSING("PROCESSING"),
    DEAD("DEAD");

    private final String value;

    InventoryAuditOutboxStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static InventoryAuditOutboxStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory audit outbox status: " + value));
    }
}
