package com.github.thundax.bacon.storage.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储对象审计日志持久化数据对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_storage_audit_log")
public class StorageAuditLogDO {

    /** 主键。 */
    private Long id;
    /** 所属租户业务键。 */
    @TableField("tenant_id")
    private TenantId tenantId;
    /** 存储对象主键。 */
    @TableField("object_id")
    private StoredObjectId objectId;
    /** 引用方类型。 */
    @TableField("owner_type")
    private String ownerType;
    /** 引用方业务主键。 */
    @TableField("owner_id")
    private String ownerId;
    /** 审计动作类型。 */
    @TableField("action_type")
    private String actionType;
    /** 变更前状态。 */
    @TableField("before_status")
    private String beforeStatus;
    /** 变更后状态。 */
    @TableField("after_status")
    private String afterStatus;
    /** 操作人类型。 */
    @TableField("operator_type")
    private String operatorType;
    /** 操作人主键。 */
    @TableField("operator_id")
    private Long operatorId;
    /** 审计发生时间。 */
    @TableField("occurred_at")
    private Instant occurredAt;
}
