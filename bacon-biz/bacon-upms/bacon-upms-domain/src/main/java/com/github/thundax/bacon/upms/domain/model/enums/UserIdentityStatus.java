package com.github.thundax.bacon.upms.domain.model.enums;

public enum UserIdentityStatus {

    ACTIVE,
    DISABLED;

    public String value() {
        return name();
    }

    public static UserIdentityStatus fromValue(String value) {
        return value == null ? null : UserIdentityStatus.valueOf(value);
    }
}
