package com.github.thundax.bacon.storage.domain.model.enums;

public enum StoredObjectStatus {

    ACTIVE,
    DELETING,
    DELETED;

    public String value() {
        return name();
    }

    public static StoredObjectStatus fromValue(String value) {
        return value == null ? null : StoredObjectStatus.valueOf(value);
    }
}
