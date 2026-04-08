package com.github.thundax.bacon.storage.domain.model.enums;

import java.util.Arrays;

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
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown storage audit outbox status: " + value));
    }
}
