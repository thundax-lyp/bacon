package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;

public final class InventoryIdMapper {

    private InventoryIdMapper() {
    }

    public static InventoryId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return InventoryId.of(value);
    }
}
