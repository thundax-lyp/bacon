package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 库存状态。
 */
public enum InventoryStatus {

    UNRESERVED("UNRESERVED"),
    RESERVING("RESERVING"),
    RESERVED("RESERVED"),
    RELEASED("RELEASED"),
    DEDUCTED("DEDUCTED"),
    FAILED("FAILED");

    private final String value;

    InventoryStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static InventoryStatus fromValue(String value) {
        for (InventoryStatus inventoryStatus : values()) {
            if (inventoryStatus.value.equals(value)) {
                return inventoryStatus;
            }
        }
        throw new IllegalArgumentException("Unsupported inventory status: " + value);
    }
}
