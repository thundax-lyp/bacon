package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 存储审计日志实体。
 */
@Getter
public class StorageAuditLog {

    public static final String ACTION_UPLOAD = "UPLOAD";
    public static final String ACTION_REFERENCE_ADD = "REFERENCE_ADD";
    public static final String ACTION_REFERENCE_CLEAR = "REFERENCE_CLEAR";
    public static final String ACTION_DELETE = "DELETE";
    public static final String OPERATOR_TYPE_SYSTEM = "SYSTEM";
    public static final Long OPERATOR_ID_SYSTEM = 0L;

    private final Long id;
    private final String tenantId;
    private final Long objectId;
    private final String ownerType;
    private final String ownerId;
    private final String actionType;
    private final String beforeStatus;
    private final String afterStatus;
    private final String operatorType;
    private final Long operatorId;
    private final Instant occurredAt;

    public StorageAuditLog(Long id, String tenantId, Long objectId, String ownerType, String ownerId, String actionType,
                           String beforeStatus, String afterStatus, String operatorType, Long operatorId,
                           Instant occurredAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.objectId = objectId;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.actionType = actionType;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.operatorType = operatorType;
        this.operatorId = operatorId;
        this.occurredAt = occurredAt;
    }
}
