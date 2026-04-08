package com.github.thundax.bacon.auth.domain.model.enums;

import java.util.Arrays;

public enum ClientStatus {

    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static ClientStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ClientStatus value: " + value));
    }
}
