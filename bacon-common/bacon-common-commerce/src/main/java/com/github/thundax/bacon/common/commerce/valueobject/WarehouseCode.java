package com.github.thundax.bacon.common.commerce.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;

public record WarehouseCode(String value) {

    public WarehouseCode {
        Objects.requireNonNull(value, "warehouseCode must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("warehouseCode must not be blank");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static WarehouseCode of(String value) {
        return new WarehouseCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
