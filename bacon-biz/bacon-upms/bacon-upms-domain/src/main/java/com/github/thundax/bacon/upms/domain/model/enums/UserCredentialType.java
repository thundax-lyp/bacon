package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum UserCredentialType {
    PASSWORD,
    TOTP;

    public String value() {
        return name();
    }

    public static UserCredentialType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user credential type: " + value));
    }
}
