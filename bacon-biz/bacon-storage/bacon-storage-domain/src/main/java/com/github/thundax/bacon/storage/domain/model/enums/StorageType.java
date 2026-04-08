package com.github.thundax.bacon.storage.domain.model.enums;

import java.util.Arrays;

public enum StorageType {

    LOCAL_FILE,
    OSS;

    public String value() {
        return name();
    }

    public static StorageType fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown storage type: " + value));
    }
}
