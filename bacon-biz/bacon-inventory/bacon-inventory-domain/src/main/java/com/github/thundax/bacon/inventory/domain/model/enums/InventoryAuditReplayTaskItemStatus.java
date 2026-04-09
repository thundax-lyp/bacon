package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryAuditReplayTaskItemStatus {
    PENDING,
    SUCCEEDED,
    FAILED;

    public String value() {
        return name();
    }

    public static InventoryAuditReplayTaskItemStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown inventory audit replay task item status: " + value));
    }
}
