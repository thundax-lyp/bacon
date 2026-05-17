package com.github.thundax.bacon.product.domain.model.enums;

import java.util.Arrays;

public enum ArchiveType {
    CREATE,
    UPDATE_BASE,
    UPDATE_SKU,
    UPDATE_IMAGE,
    STATUS_CHANGE,
    ARCHIVE;

    public String value() {
        return name();
    }

    public static ArchiveType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown archive type: " + value));
    }
}
