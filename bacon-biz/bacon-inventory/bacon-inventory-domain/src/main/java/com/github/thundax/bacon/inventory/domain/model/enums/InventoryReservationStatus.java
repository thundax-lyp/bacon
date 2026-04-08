package com.github.thundax.bacon.inventory.domain.model.enums;

public enum InventoryReservationStatus {

    CREATED,
    RESERVED,
    RELEASED,
    DEDUCTED,
    FAILED;

    public String value() {
        return name();
    }

    public static InventoryReservationStatus fromValue(String value) {
        return InventoryReservationStatus.valueOf(value);
    }
}
