package com.github.thundax.bacon.upms.domain.model.enums;

public enum UserCredentialType {

    PASSWORD,
    TOTP;

    public String value() {
        return name();
    }

    public static UserCredentialType fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
