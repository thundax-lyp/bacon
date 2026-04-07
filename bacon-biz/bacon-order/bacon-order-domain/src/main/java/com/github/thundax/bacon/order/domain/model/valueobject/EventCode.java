package com.github.thundax.bacon.order.domain.model.valueobject;

/**
 * 出站事件业务标识。
 */
public record EventCode(String value) {

    public EventCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("eventCode must not be blank");
        }
    }

    public static EventCode of(String value) {
        return new EventCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
