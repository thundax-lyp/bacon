package com.github.thundax.bacon.order.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单出站事件死信记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutboxDeadLetter {

    public static final String REPLAY_STATUS_PENDING = "PENDING";
    public static final String REPLAY_STATUS_SUCCESS = "SUCCESS";
    public static final String REPLAY_STATUS_FAILED = "FAILED";

    /** 死信记录主键。 */
    private Long id;
    /** 出站事件主键。 */
    private Long outboxId;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 事件类型。 */
    private String eventType;
    /** 业务幂等键。 */
    private String businessKey;
    /** 事件载荷。 */
    private String payload;
    /** 重试次数。 */
    private Integer retryCount;
    /** 错误信息。 */
    private String errorMessage;
    /** 死信原因。 */
    private String deadReason;
    /** 死信时间。 */
    private Instant deadAt;
    /** 回放状态。 */
    private String replayStatus;
    /** 回放次数。 */
    private Integer replayCount;
    /** 最近一次回放时间。 */
    private Instant lastReplayAt;
    /** 最近一次回放结果信息。 */
    private String lastReplayMessage;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;
}
