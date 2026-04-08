package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum UserIdentityStatus {

    ACTIVE,
    DISABLED;

    public String value() {
        return name();
    }

    public static UserIdentityStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user identity status: " + value));
    }
}
