package com.github.thundax.bacon.inventory.domain.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Inventory {

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";

    private Long id;
    private Long tenantId;
    private Long skuId;
    private Long warehouseId;
    private Integer onHandQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String status;
    private Instant updatedAt;

    public void reserve(int quantity, Instant operatedAt) {
        ensureReservable(quantity);
        reservedQuantity += quantity;
        refreshAvailableQuantity(operatedAt);
    }

    public void ensureReservable(int quantity) {
        validateQuantity(quantity);
        ensureEnabled();
        if (availableQuantity < quantity) {
            throw new IllegalArgumentException("INSUFFICIENT_STOCK:" + skuId);
        }
    }

    public void release(int quantity, Instant operatedAt) {
        validateQuantity(quantity);
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("RESERVED_QUANTITY_NOT_ENOUGH:" + skuId);
        }
        reservedQuantity -= quantity;
        refreshAvailableQuantity(operatedAt);
    }

    public void deduct(int quantity, Instant operatedAt) {
        validateQuantity(quantity);
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("RESERVED_QUANTITY_NOT_ENOUGH:" + skuId);
        }
        if (onHandQuantity < quantity) {
            throw new IllegalStateException("ON_HAND_QUANTITY_NOT_ENOUGH:" + skuId);
        }
        reservedQuantity -= quantity;
        onHandQuantity -= quantity;
        refreshAvailableQuantity(operatedAt);
    }

    private void ensureEnabled() {
        if (STATUS_DISABLED.equals(status)) {
            throw new IllegalArgumentException("INVENTORY_DISABLED:" + skuId);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("INVALID_QUANTITY:" + skuId);
        }
    }

    private void refreshAvailableQuantity(Instant operatedAt) {
        availableQuantity = onHandQuantity - reservedQuantity;
        updatedAt = operatedAt;
    }
}
