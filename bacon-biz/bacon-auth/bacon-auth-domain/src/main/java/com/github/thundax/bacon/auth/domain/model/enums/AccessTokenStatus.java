package com.github.thundax.bacon.auth.domain.model.enums;

import java.util.Arrays;

public enum AccessTokenStatus {

    ACTIVE,
    REVOKED;

    public String value() {
        return name();
    }

    public static AccessTokenStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown AccessTokenStatus value: " + value));
    }
}
