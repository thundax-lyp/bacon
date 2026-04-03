package com.github.thundax.bacon.order.infra.persistence.dataobject;

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
@TableName("bacon_order_outbox")
public class OrderOutboxEventDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private Long tenantId;
    @TableField("order_no")
    private String orderNo;
    @TableField("event_type")
    private String eventType;
    @TableField("business_key")
    private String businessKey;
    @TableField("payload")
    private String payload;
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
    @TableField("error_message")
    private String errorMessage;
    @TableField("dead_reason")
    private String deadReason;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}
