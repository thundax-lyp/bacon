package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum UserCredentialFactorLevel {
    PRIMARY,
    SECONDARY;

    public String value() {
        return name();
    }

    public static UserCredentialFactorLevel from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user credential factor level: " + value));
    }
}
