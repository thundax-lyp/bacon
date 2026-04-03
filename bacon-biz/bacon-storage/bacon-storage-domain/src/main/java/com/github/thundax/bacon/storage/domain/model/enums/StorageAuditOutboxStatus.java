package com.github.thundax.bacon.storage.domain.model.enums;

/**
 * 存储审计补偿出站状态。
 */
public enum StorageAuditOutboxStatus {

    NEW,
    RETRYING,
    PROCESSING,
    DEAD;

    public String value() {
        return name();
    }

    public static StorageAuditOutboxStatus fromValue(String value) {
        return value == null ? null : StorageAuditOutboxStatus.valueOf(value);
    }
}
