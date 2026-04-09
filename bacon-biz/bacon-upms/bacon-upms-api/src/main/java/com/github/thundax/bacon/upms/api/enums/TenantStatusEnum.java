package com.github.thundax.bacon.upms.api.enums;

public enum TenantStatusEnum {
    ACTIVE,
    DISABLED,
    EXPIRED;

    public String value() {
        return name();
    }
}
