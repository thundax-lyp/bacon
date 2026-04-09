package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum PostStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static PostStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown post status: " + value));
    }
}
