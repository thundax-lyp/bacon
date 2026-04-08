package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum RoleType {

    SYSTEM_ROLE,
    TENANT_ROLE,
    CUSTOM_ROLE;

    public String value() {
        return name();
    }

    public static RoleType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role type: " + value));
    }
}
