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
@TableName("bacon_inventory_audit_outbox")
public class InventoryAuditOutboxDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("event_code")
    private String eventCode;
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
    @TableField("error_message")
    private String errorMessage;
    @TableField("status")
    private String status;
    @TableField("retry_count")
    private Integer retryCount;
    @TableField("next_retry_at")
    private Instant nextRetryAt;
    @TableField("processing_owner")
    private String processingOwner;
    @TableField("lease_until")
    private Instant leaseUntil;
    @TableField("claimed_at")
    private Instant claimedAt;
    @TableField("dead_reason")
    private String deadReason;
    @TableField("failed_at")
    private Instant failedAt;
    @TableField("updated_at")
    private Instant updatedAt;
}
