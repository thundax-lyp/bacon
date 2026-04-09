package com.github.thundax.bacon.order.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_order_idempotency_record")
public class OrderIdempotencyRecordDO {

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("order_no")
    private String orderNo;

    @TableField("event_type")
    private String eventType;

    @TableField("status")
    private String status;

    @TableField("attempt_count")
    private Integer attemptCount;

    @TableField("last_error")
    private String lastError;

    @TableField("processing_owner")
    private String processingOwner;

    @TableField("lease_until")
    private Instant leaseUntil;

    @TableField("claimed_at")
    private Instant claimedAt;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
