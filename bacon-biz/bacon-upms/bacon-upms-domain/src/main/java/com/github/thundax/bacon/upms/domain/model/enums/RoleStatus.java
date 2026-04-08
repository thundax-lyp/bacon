package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum RoleStatus {

    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static RoleStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role status: " + value));
    }
}
