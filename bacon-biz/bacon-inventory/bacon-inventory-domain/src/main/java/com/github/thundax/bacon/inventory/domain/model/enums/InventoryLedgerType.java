package com.github.thundax.bacon.inventory.domain.model.enums;

public enum InventoryLedgerType {

    RESERVE,
    RELEASE,
    DEDUCT;

    public String value() {
        return name();
    }

    public static InventoryLedgerType fromValue(String value) {
        return InventoryLedgerType.valueOf(value);
    }
}
