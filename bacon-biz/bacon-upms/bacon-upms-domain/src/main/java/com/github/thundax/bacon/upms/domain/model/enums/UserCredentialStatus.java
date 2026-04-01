package com.github.thundax.bacon.upms.domain.model.enums;

public enum UserCredentialStatus {

    ACTIVE,
    LOCKED,
    EXPIRED,
    DISABLED;

    public String value() {
        return name();
    }
}
