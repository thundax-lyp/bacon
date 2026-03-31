package com.github.thundax.bacon.upms.domain.model.enums;

public enum UserStatus {

    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }
}
