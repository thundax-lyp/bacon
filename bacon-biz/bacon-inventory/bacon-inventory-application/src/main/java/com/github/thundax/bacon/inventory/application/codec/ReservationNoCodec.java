package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;

public final class ReservationNoCodec {

    private ReservationNoCodec() {
    }

    public static ReservationNo toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ReservationNo.of(value);
    }

    public static String toValue(ReservationNo reservationNo) {
        return reservationNo == null ? null : reservationNo.value();
    }
}
