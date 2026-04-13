package com.github.thundax.bacon.storage.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 存储审计日志实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageAuditLog {

    public static final String OPERATOR_TYPE_SYSTEM = "SYSTEM";
    public static final Long OPERATOR_ID_SYSTEM = 0L;

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

    public static StorageAuditLog create(
            Long id,
            StoredObjectId objectId,
            String ownerType,
            String ownerId,
            StorageAuditActionType actionType,
            String beforeStatus,
            String afterStatus,
            String operatorType,
            Long operatorId,
            Instant occurredAt) {
        return new StorageAuditLog(
                id,
                objectId,
                ownerType,
                ownerId,
                actionType,
                beforeStatus,
                afterStatus,
                operatorType,
                operatorId,
                occurredAt);
    }

    public static StorageAuditLog reconstruct(
            Long id,
            StoredObjectId objectId,
            String ownerType,
            String ownerId,
            StorageAuditActionType actionType,
            String beforeStatus,
            String afterStatus,
            String operatorType,
            Long operatorId,
            Instant occurredAt) {
        return new StorageAuditLog(
                id,
                objectId,
                ownerType,
                ownerId,
                actionType,
                beforeStatus,
                afterStatus,
                operatorType,
                operatorId,
                occurredAt);
    }
}
