package com.github.thundax.bacon.inventory.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_inventory_audit_replay_task")
public class InventoryAuditReplayTaskDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("task_no")
    private String taskNo;

    @TableField("status")
    private String status;

    @TableField("total_count")
    private Integer totalCount;

    @TableField("processed_count")
    private Integer processedCount;

    @TableField("success_count")
    private Integer successCount;

    @TableField("failed_count")
    private Integer failedCount;

    @TableField("replay_key_prefix")
    private String replayKeyPrefix;

    @TableField("operator_type")
    private String operatorType;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("processing_owner")
    private String processingOwner;

    @TableField("lease_until")
    private Instant leaseUntil;

    @TableField("last_error")
    private String lastError;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("started_at")
    private Instant startedAt;

    @TableField("paused_at")
    private Instant pausedAt;

    @TableField("finished_at")
    private Instant finishedAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
