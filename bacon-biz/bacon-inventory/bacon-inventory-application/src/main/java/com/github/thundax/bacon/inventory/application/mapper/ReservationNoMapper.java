package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;

public final class ReservationNoMapper {

    private ReservationNoMapper() {
    }

    public static ReservationNo toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ReservationNo.of(value);
    }
}
