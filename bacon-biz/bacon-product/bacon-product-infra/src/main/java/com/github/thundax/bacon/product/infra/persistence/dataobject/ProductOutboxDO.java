package com.github.thundax.bacon.product.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_product_outbox")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class ProductOutboxDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("aggregate_id")
    private Long aggregateId;

    @TableField("aggregate_type")
    private String aggregateType;

    @TableField("event_type")
    private String eventType;

    @TableField("product_version")
    private Long productVersion;

    @TableField("payload")
    private String payload;

    @TableField("outbox_status")
    private String outboxStatus;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("next_retry_at")
    private Instant nextRetryAt;

    @TableField("processing_owner")
    private String processingOwner;

    @TableField("lease_until")
    private Instant leaseUntil;

    @TableField("failure_reason")
    private String failureReason;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
