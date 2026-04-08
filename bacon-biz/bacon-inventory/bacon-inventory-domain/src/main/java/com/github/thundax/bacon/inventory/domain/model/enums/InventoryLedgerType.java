package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryLedgerType {

    RESERVE,
    RELEASE,
    DEDUCT;

    public String value() {
        return name();
    }

    public static InventoryLedgerType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory ledger type: " + value));
    }
}
