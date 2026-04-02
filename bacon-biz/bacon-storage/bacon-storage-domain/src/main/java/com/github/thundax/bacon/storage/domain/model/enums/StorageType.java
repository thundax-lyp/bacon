package com.github.thundax.bacon.storage.domain.model.enums;

public enum StorageType {

    LOCAL_FILE,
    OSS;

    public String value() {
        return name();
    }

    public static StorageType fromValue(String value) {
        return value == null ? null : StorageType.valueOf(value);
    }
}
