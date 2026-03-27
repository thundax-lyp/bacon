package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 存储审计补偿出站事件。
 */
@Getter
public class StorageAuditOutbox {

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_RETRYING = "RETRYING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_DEAD = "DEAD";

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
    private final String errorMessage;
    private final String status;
    private final Integer retryCount;
    private final Instant nextRetryAt;
    private final Instant updatedAt;

    public StorageAuditOutbox(Long id, String tenantId, Long objectId, String ownerType, String ownerId, String actionType,
                              String beforeStatus, String afterStatus, String operatorType, Long operatorId,
                              Instant occurredAt, String errorMessage, String status, Integer retryCount,
                              Instant nextRetryAt, Instant updatedAt) {
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
        this.errorMessage = errorMessage;
        this.status = status;
        this.retryCount = retryCount;
        this.nextRetryAt = nextRetryAt;
        this.updatedAt = updatedAt;
    }

    public static StorageAuditOutbox newEvent(StorageAuditLog auditLog, String errorMessage, Instant now) {
        return new StorageAuditOutbox(null, auditLog.getTenantId(), auditLog.getObjectId(), auditLog.getOwnerType(),
                auditLog.getOwnerId(), auditLog.getActionType(), auditLog.getBeforeStatus(), auditLog.getAfterStatus(),
                auditLog.getOperatorType(), auditLog.getOperatorId(), auditLog.getOccurredAt(), errorMessage,
                STATUS_NEW, 0, now, now);
    }
}
