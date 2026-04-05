package com.github.thundax.bacon.upms.domain.model.enums;

public enum DepartmentStatus {

    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static DepartmentStatus fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
