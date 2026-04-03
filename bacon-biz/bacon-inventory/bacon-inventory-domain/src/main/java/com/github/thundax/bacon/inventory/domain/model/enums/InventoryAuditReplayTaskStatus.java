package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryAuditReplayTaskStatus {

    PENDING("PENDING"),
    RUNNING("RUNNING"),
    PAUSED("PAUSED"),
    SUCCEEDED("SUCCEEDED"),
    FAILED("FAILED"),
    CANCELED("CANCELED");

    private final String value;

    InventoryAuditReplayTaskStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static InventoryAuditReplayTaskStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory audit replay task status: " + value));
    }
}
