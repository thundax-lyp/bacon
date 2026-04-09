package com.github.thundax.bacon.auth.domain.model.enums;

import java.util.Arrays;

public enum RefreshTokenSessionStatus {
    ACTIVE,
    USED,
    INVALIDATED,
    EXPIRED;

    public String value() {
        return name();
    }

    public static RefreshTokenSessionStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown RefreshTokenSessionStatus value: " + value));
    }
}
