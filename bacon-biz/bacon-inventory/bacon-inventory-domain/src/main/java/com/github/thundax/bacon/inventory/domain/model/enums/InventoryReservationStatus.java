package com.github.thundax.bacon.inventory.domain.model.enums;

import java.util.Arrays;

public enum InventoryReservationStatus {

    CREATED,
    RESERVED,
    RELEASED,
    DEDUCTED,
    FAILED;

    public String value() {
        return name();
    }

    public static InventoryReservationStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory reservation status: " + value));
    }
}
