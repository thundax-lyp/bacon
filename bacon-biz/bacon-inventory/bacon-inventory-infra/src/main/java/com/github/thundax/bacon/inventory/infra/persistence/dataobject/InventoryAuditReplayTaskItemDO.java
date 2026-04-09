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
@TableName("bacon_inventory_audit_replay_task_item")
public class InventoryAuditReplayTaskItemDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("dead_letter_id")
    private Long deadLetterId;

    @TableField("item_status")
    private String itemStatus;

    @TableField("replay_status")
    private String replayStatus;

    @TableField("replay_key")
    private String replayKey;

    @TableField("result_message")
    private String resultMessage;

    @TableField("started_at")
    private Instant startedAt;

    @TableField("finished_at")
    private Instant finishedAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
