package com.github.thundax.bacon.inventory.domain.model.enums;

public enum InventoryAuditReplayTaskItemStatus {

    PENDING,
    SUCCEEDED,
    FAILED;

    public String value() {
        return name();
    }

    public static InventoryAuditReplayTaskItemStatus fromValue(String value) {
        return InventoryAuditReplayTaskItemStatus.valueOf(value);
    }
}
