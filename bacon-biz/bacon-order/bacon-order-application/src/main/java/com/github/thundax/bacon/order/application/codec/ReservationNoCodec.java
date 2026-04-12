package com.github.thundax.bacon.order.application.codec;

import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;

public final class ReservationNoCodec {

    private ReservationNoCodec() {}

    public static ReservationNo toDomain(String value) {
        return value == null ? null : ReservationNo.of(value);
    }

    public static String toValue(ReservationNo value) {
        return value == null ? null : value.value();
    }
}
