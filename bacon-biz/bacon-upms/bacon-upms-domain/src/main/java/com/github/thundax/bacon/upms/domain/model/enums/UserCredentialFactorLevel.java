package com.github.thundax.bacon.upms.domain.model.enums;

public enum UserCredentialFactorLevel {

    PRIMARY,
    SECONDARY;

    public String value() {
        return name();
    }

    public static UserCredentialFactorLevel fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
