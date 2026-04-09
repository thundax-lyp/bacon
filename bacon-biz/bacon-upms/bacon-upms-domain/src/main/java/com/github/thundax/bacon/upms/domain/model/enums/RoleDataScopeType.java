package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum RoleDataScopeType {
    ALL,
    DEPARTMENT,
    DEPARTMENT_AND_CHILDREN,
    SELF,
    CUSTOM;

    public String value() {
        return name();
    }

    public static RoleDataScopeType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role data scope type: " + value));
    }
}
