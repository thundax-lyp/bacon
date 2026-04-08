package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryAuditReplayTaskStatus {
    PENDING,
    RUNNING,
    PAUSED,
    SUCCEEDED,
    FAILED,
    CANCELED;

    public String value() {
        return name();
    }

    public static InventoryAuditReplayTaskStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory audit replay task status: " + value));
    }
}
