package com.github.thundax.bacon.storage.domain.model.valueobject;

/**
 * 存储对象外部编号（稳定外部表达）。
 */
public record StoredObjectNo(String value) {

    public StoredObjectNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("storedObjectNo must not be blank");
        }
    }

    public static StoredObjectNo of(String value) {
        return new StoredObjectNo(value.trim());
    }
}
