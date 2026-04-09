package com.github.thundax.bacon.storage.domain.model.enums;

import java.util.Arrays;

public enum StoredObjectStatus {
    ACTIVE,
    DELETING,
    DELETED;

    public String value() {
        return name();
    }

    public static StoredObjectStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown stored object status: " + value));
    }
}
