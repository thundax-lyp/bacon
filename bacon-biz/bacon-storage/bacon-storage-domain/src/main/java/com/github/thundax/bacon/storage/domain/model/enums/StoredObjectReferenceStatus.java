package com.github.thundax.bacon.storage.domain.model.enums;

import java.util.Arrays;

public enum StoredObjectReferenceStatus {

    UNREFERENCED,
    REFERENCED;

    public String value() {
        return name();
    }

    public static StoredObjectReferenceStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown stored object reference status: " + value));
    }
}
