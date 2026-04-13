package com.github.thundax.bacon.storage.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditOutboxStatus;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 存储审计补偿出站事件。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageAuditOutbox {

    /** 主键。 */
    private Long id;
    /** 存储对象主键。 */
    private StoredObjectId objectId;
    /** 引用方类型。 */
    private String ownerType;
    /** 引用方业务主键。 */
    private String ownerId;
    /** 审计动作类型。 */
    private StorageAuditActionType actionType;
    /** 变更前状态。 */
    private String beforeStatus;
    /** 变更后状态。 */
    private String afterStatus;
    /** 操作人类型。 */
    private String operatorType;
    /** 操作人主键。 */
    private Long operatorId;
    /** 审计发生时间。 */
    private Instant occurredAt;
    /** 最近一次失败原因。 */
    private String errorMessage;
    /** 补偿状态。 */
    private StorageAuditOutboxStatus status;
    /** 已重试次数。 */
    private Integer retryCount;
    /** 下次重试时间。 */
    private Instant nextRetryAt;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static StorageAuditOutbox create(
            Long id,
            StoredObjectId objectId,
            String ownerType,
            String ownerId,
            StorageAuditActionType actionType,
            String beforeStatus,
            String afterStatus,
            String operatorType,
            Long operatorId,
            Instant occurredAt,
            String errorMessage,
            StorageAuditOutboxStatus status,
            Integer retryCount,
            Instant nextRetryAt,
            Instant updatedAt) {
        return new StorageAuditOutbox(
                id,
                objectId,
                ownerType,
                ownerId,
                actionType,
                beforeStatus,
                afterStatus,
                operatorType,
                operatorId,
                occurredAt,
                errorMessage,
                status,
                retryCount,
                nextRetryAt,
                updatedAt);
    }

    public static StorageAuditOutbox reconstruct(
            Long id,
            StoredObjectId objectId,
            String ownerType,
            String ownerId,
            StorageAuditActionType actionType,
            String beforeStatus,
            String afterStatus,
            String operatorType,
            Long operatorId,
            Instant occurredAt,
            String errorMessage,
            StorageAuditOutboxStatus status,
            Integer retryCount,
            Instant nextRetryAt,
            Instant updatedAt) {
        return new StorageAuditOutbox(
                id,
                objectId,
                ownerType,
                ownerId,
                actionType,
                beforeStatus,
                afterStatus,
                operatorType,
                operatorId,
                occurredAt,
                errorMessage,
                status,
                retryCount,
                nextRetryAt,
                updatedAt);
    }
}
