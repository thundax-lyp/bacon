package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;

public final class EventCodeMapper {

    private EventCodeMapper() {
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
