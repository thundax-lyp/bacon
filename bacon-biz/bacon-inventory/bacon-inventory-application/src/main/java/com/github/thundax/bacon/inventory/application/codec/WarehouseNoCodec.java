package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.WarehouseNo;

public final class WarehouseNoCodec {

    private WarehouseNoCodec() {
    }

    public static WarehouseNo toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return WarehouseNo.of(value);
    }

    public static String toValue(WarehouseNo warehouseNo) {
        return warehouseNo == null ? null : warehouseNo.value();
    }
}
