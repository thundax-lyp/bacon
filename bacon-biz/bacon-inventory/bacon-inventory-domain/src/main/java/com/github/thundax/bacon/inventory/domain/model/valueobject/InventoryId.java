package com.github.thundax.bacon.inventory.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class InventoryId extends BaseLongId {

    private InventoryId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InventoryId of(Long value) {
        return new InventoryId(value);
    }
}
