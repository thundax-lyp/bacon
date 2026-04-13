package com.github.thundax.bacon.order.infra.persistence.dataobject;

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
@TableName("bacon_order_dead_letter")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class OrderOutboxDeadLetterDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("outbox_id")
    private Long outboxId;

    @TableField("event_code")
    private String eventCode;

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

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("error_message")
    private String errorMessage;

    @TableField("dead_reason")
    private String deadReason;

    @TableField("dead_at")
    private Instant deadAt;

    @TableField("replay_status")
    private String replayStatus;

    @TableField("replay_count")
    private Integer replayCount;

    @TableField("last_replay_at")
    private Instant lastReplayAt;

    @TableField("last_replay_message")
    private String lastReplayMessage;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
