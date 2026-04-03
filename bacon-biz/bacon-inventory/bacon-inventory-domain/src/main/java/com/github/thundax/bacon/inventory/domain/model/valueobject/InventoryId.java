package com.github.thundax.bacon.inventory.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class InventoryId extends BaseStringId {

    private InventoryId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InventoryId of(String value) {
        return new InventoryId(value);
    }
}
