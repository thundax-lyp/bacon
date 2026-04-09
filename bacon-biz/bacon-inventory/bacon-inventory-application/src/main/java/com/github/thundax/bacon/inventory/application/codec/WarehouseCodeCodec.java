package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;

public final class WarehouseCodeCodec {

    private WarehouseCodeCodec() {}

    public static WarehouseCode toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return WarehouseCode.of(value);
    }

    public static String toValue(WarehouseCode warehouseCode) {
        return warehouseCode == null ? null : warehouseCode.value();
    }
}
