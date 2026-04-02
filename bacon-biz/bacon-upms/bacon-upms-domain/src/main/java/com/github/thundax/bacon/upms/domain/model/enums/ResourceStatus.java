package com.github.thundax.bacon.upms.domain.model.enums;

public enum ResourceStatus {

    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static ResourceStatus fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
