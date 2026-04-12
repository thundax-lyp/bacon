package com.github.thundax.bacon.order.application.codec;

import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;

public final class EventCodeCodec {

    private EventCodeCodec() {}

    public static EventCode toDomain(String value) {
        return value == null ? null : EventCode.of(value);
    }

    public static String toValue(EventCode value) {
        return value == null ? null : value.value();
    }
}
