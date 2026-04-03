package com.github.thundax.bacon.storage.domain.model.enums;

public enum UploadStatus {

    INITIATED,
    UPLOADING,
    COMPLETED,
    ABORTED;

    public String value() {
        return name();
    }

    public static UploadStatus fromValue(String value) {
        return value == null ? null : UploadStatus.valueOf(value);
    }
}
