package com.github.thundax.bacon.upms.api.enums;

public enum UpmsStatusEnum {

    ENABLED,
    DISABLED;

    public boolean matches(String value) {
        return value != null && name().equalsIgnoreCase(value.trim());
    }

    public String value() {
        return name();
    }
}
