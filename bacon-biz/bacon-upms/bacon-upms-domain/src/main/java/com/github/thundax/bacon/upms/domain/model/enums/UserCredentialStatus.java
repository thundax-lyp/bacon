package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum UserCredentialStatus {
    ACTIVE,
    LOCKED,
    EXPIRED,
    DISABLED;

    public String value() {
        return name();
    }

    public static UserCredentialStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user credential status: " + value));
    }
}
