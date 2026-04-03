package com.github.thundax.bacon.order.domain.model.valueobject;

/**
 * 仓库业务编号。
 */
public record WarehouseNo(String value) {

    public WarehouseNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("warehouseNo must not be blank");
        }
    }

    public static WarehouseNo of(String value) {
        return new WarehouseNo(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
