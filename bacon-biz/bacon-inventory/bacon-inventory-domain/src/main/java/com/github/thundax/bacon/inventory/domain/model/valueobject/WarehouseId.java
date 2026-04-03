package com.github.thundax.bacon.inventory.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class WarehouseId extends BaseStringId {

    private WarehouseId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static WarehouseId of(String value) {
        return new WarehouseId(value);
    }
}
