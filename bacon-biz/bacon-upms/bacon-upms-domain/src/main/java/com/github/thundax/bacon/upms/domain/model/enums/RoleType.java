package com.github.thundax.bacon.upms.domain.model.enums;

public enum RoleType {

    SYSTEM_ROLE,
    TENANT_ROLE,
    CUSTOM_ROLE;

    public String value() {
        return name();
    }

    public static RoleType fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
