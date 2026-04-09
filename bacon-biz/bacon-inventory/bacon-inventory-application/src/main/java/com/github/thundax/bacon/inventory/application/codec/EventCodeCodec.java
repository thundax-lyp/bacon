package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;

public final class EventCodeCodec {

    private EventCodeCodec() {
    }

    public static EventCode toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return EventCode.of(value);
    }

    public static String toValue(EventCode eventCode) {
        return eventCode == null ? null : eventCode.value();
    }
}
