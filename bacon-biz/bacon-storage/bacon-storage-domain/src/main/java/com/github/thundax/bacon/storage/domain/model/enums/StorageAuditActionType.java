package com.github.thundax.bacon.storage.domain.model.enums;

import java.util.Arrays;

/**
 * 存储审计动作类型。
 */
public enum StorageAuditActionType {
    UPLOAD,
    REFERENCE_ADD,
    REFERENCE_CLEAR,
    DELETE;

    public String value() {
        return name();
    }

    public static StorageAuditActionType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown storage audit action type: " + value));
    }
}
