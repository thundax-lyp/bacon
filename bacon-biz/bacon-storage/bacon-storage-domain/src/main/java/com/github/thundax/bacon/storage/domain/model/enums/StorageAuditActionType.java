package com.github.thundax.bacon.storage.domain.model.enums;

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

    public static StorageAuditActionType fromValue(String value) {
        return value == null ? null : StorageAuditActionType.valueOf(value);
    }
}
