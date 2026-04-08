package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;

public final class OrderNoMapper {

    private OrderNoMapper() {
    }

    public static OrderNo toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return OrderNo.of(value);
    }
}
