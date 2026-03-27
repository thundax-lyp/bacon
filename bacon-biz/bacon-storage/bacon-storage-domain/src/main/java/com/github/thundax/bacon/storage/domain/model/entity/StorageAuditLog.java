package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 存储审计日志实体。
 */
@Getter
public class StorageAuditLog {

    public static final String ACTION_UPLOAD = "UPLOAD";
    public static final String ACTION_MULTIPART_INIT = "MULTIPART_INIT";
    public static final String ACTION_MULTIPART_UPLOAD_PART = "MULTIPART_UPLOAD_PART";
    public static final String ACTION_MULTIPART_COMPLETE = "MULTIPART_COMPLETE";
    public static final String ACTION_MULTIPART_ABORT = "MULTIPART_ABORT";
    public static final String ACTION_REFERENCE_ADD = "REFERENCE_ADD";
    public static final String ACTION_REFERENCE_CLEAR = "REFERENCE_CLEAR";
    public static final String ACTION_DELETE = "DELETE";
    public static final String OPERATOR_TYPE_SYSTEM = "SYSTEM";
    public static final Long OPERATOR_ID_SYSTEM = 0L;

    /** 主键。 */
    private Long id;
    /** 所属租户业务键。 */
    private String tenantId;
    /** 存储对象主键。 */
    private Long objectId;
    /** 引用方类型。 */
    private String ownerType;
    /** 引用方业务主键。 */
    private String ownerId;
    /** 审计动作类型。 */
    private String actionType;
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

    public static StorageAuditLog systemAction(String tenantId, Long objectId, String ownerType, String ownerId,
                                               String actionType, String beforeStatus, String afterStatus) {
        return new StorageAuditLog(null, tenantId, objectId, ownerType, ownerId, actionType, beforeStatus, afterStatus,
                OPERATOR_TYPE_SYSTEM, OPERATOR_ID_SYSTEM, Instant.now());
    }
}
