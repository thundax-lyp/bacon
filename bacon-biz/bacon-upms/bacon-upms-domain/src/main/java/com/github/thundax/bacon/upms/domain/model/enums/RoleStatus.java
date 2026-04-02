package com.github.thundax.bacon.upms.domain.model.enums;

public enum RoleStatus {

    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static RoleStatus fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
