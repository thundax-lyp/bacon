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
@TableName("bacon_inventory_audit_dead_letter")
public class InventoryAuditDeadLetterDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("outbox_id")
    private Long outboxId;
    @TableField("tenant_id")
    private Long tenantId;
    @TableField("order_no")
    private String orderNo;
    @TableField("reservation_no")
    private String reservationNo;
    @TableField("action_type")
    private String actionType;
    @TableField("operator_type")
    private String operatorType;
    @TableField("operator_id")
    private Long operatorId;
    @TableField("occurred_at")
    private Instant occurredAt;
    @TableField("retry_count")
    private Integer retryCount;
    @TableField("error_message")
    private String errorMessage;
    @TableField("dead_reason")
    private String deadReason;
    @TableField("dead_at")
    private Instant deadAt;
}
