package com.github.thundax.bacon.storage.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_storage_audit_log")
public class StorageAuditLogDO {

    private Long id;
    @TableField("tenant_id")
    private String tenantId;
    @TableField("object_id")
    private Long objectId;
    @TableField("owner_type")
    private String ownerType;
    @TableField("owner_id")
    private String ownerId;
    @TableField("action_type")
    private String actionType;
    @TableField("before_status")
    private String beforeStatus;
    @TableField("after_status")
    private String afterStatus;
    @TableField("operator_type")
    private String operatorType;
    @TableField("operator_id")
    private Long operatorId;
    @TableField("occurred_at")
    private Instant occurredAt;
}
