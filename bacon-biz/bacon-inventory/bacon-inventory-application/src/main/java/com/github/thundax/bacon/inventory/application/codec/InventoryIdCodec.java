package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;

public final class InventoryIdCodec {

    private InventoryIdCodec() {
    }

    public static InventoryId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return InventoryId.of(value);
    }

    public static Long toValue(InventoryId inventoryId) {
        return inventoryId == null ? null : inventoryId.value();
    }
}
