package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryAuditOperatorType {
    SYSTEM,
    MANUAL;

    public String value() {
        return name();
    }

    public static InventoryAuditOperatorType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory audit operator type: " + value));
    }
}
