package com.github.thundax.bacon.storage.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditOutboxStatus;
import lombok.Getter;

import java.time.Instant;

/**
 * 存储审计补偿出站事件。
 */
@Getter
public class StorageAuditOutbox {

    private final Long id;
    private final TenantId tenantId;
    private final StoredObjectId objectId;
    private final String ownerType;
    private final String ownerId;
    private final StorageAuditActionType actionType;
    private final String beforeStatus;
    private final String afterStatus;
    private final String operatorType;
    private final Long operatorId;
    private final Instant occurredAt;
    private final String errorMessage;
    private final StorageAuditOutboxStatus status;
    private final Integer retryCount;
    private final Instant nextRetryAt;
    private final Instant updatedAt;

    public StorageAuditOutbox(Long id, TenantId tenantId, StoredObjectId objectId, String ownerType, String ownerId,
                              StorageAuditActionType actionType,
                              String beforeStatus, String afterStatus, String operatorType, Long operatorId,
                              Instant occurredAt, String errorMessage, StorageAuditOutboxStatus status, Integer retryCount,
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
                StorageAuditOutboxStatus.NEW, 0, now, now);
    }
}
