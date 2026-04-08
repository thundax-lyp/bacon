package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryAuditOutboxStatus {
    NEW,
    RETRYING,
    PROCESSING,
    DEAD;

    public String value() {
        return name();
    }

    public static InventoryAuditOutboxStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory audit outbox status: " + value));
    }
}
