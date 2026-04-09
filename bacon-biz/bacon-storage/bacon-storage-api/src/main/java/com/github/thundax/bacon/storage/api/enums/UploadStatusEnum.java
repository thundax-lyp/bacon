package com.github.thundax.bacon.storage.api.enums;

import java.util.Arrays;

/**
 * 分段上传状态。
 */
public enum UploadStatusEnum {
    INITIATED,
    UPLOADING,
    COMPLETED,
    ABORTED;

    public static UploadStatusEnum from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown upload status enum: " + value));
    }
}
