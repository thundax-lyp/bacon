package com.github.thundax.bacon.storage.domain.model.enums;

import java.util.Arrays;

public enum UploadStatus {

    INITIATED,
    UPLOADING,
    COMPLETED,
    ABORTED;

    public String value() {
        return name();
    }

    public static UploadStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown upload status: " + value));
    }
}
