package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryAuditActionType {
    RESERVE,
    RESERVE_FAILED,
    RELEASE,
    DEDUCT,
    AUDIT_REPLAY_SUCCEEDED,
    AUDIT_REPLAY_FAILED;

    public String value() {
        return name();
    }

    public static InventoryAuditActionType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory audit action type: " + value));
    }
}
