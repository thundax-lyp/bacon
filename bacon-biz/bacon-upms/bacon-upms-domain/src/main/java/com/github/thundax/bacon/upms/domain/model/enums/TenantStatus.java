package com.github.thundax.bacon.upms.domain.model.enums;

public enum TenantStatus {

    ACTIVE,
    EXPIRED,
    DISABLED;

    public String value() {
        return name();
    }

    public static TenantStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tenant status must not be blank");
        }
        return valueOf(value.trim().toUpperCase());
    }
}
