package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum DepartmentStatus {

    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static DepartmentStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown department status: " + value));
    }
}
