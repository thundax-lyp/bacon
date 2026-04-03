package com.github.thundax.bacon.order.domain.model.valueobject;

/**
 * 出站事件业务标识。
 */
public record EventId(String value) {

    public EventId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
    }

    public static EventId of(String value) {
        return new EventId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
