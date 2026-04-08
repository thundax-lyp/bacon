package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.WarehouseNo;

public final class WarehouseNoMapper {

    private WarehouseNoMapper() {
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
