package com.github.thundax.bacon.auth.domain.model.enums;

import java.util.Arrays;

public enum SessionStatus {
    ACTIVE,
    LOGGED_OUT,
    INVALIDATED,
    EXPIRED;

    public String value() {
        return name();
    }

    public static SessionStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown SessionStatus value: " + value));
    }
}
