package com.github.thundax.bacon.storage.domain.model.enums;

public enum StoredObjectReferenceStatus {

    UNREFERENCED,
    REFERENCED;

    public String value() {
        return name();
    }

    public static StoredObjectReferenceStatus fromValue(String value) {
        return value == null ? null : StoredObjectReferenceStatus.valueOf(value);
    }
}
