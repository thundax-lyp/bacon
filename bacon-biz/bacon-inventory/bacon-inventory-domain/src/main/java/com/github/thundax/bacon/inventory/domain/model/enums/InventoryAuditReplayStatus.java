package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryAuditReplayStatus {
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED;

    public String value() {
        return name();
    }

    public static InventoryAuditReplayStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory audit replay status: " + value));
    }
}
