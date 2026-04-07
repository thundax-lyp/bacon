package com.github.thundax.bacon.inventory.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class WarehouseNo extends BaseStringId {

    private WarehouseNo(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static WarehouseNo of(String value) {
        return new WarehouseNo(value);
    }
}
