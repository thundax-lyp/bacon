package com.github.thundax.bacon.inventory.domain.model.enums;

public enum InventoryAuditReplayStatus {

    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED;

    public String value() {
        return name();
    }

    public static InventoryAuditReplayStatus fromValue(String value) {
        return InventoryAuditReplayStatus.valueOf(value);
    }
}
