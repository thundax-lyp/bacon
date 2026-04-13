package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 订单出站事件死信记录。
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderOutboxDeadLetter {

    /** 死信记录主键。 */
    private Long id;
    /** 出站事件主键。 */
    private OutboxId outboxId;
    /** 出站事件业务标识。 */
    private EventCode eventCode;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 事件类型。 */
    private OrderOutboxEventType eventType;
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
    private OrderOutboxReplayStatus replayStatus;
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

    public static OrderOutboxDeadLetter create(
            Long id,
            Long outboxId,
            String eventCode,
            String orderNo,
            OrderOutboxEventType eventType,
            String businessKey,
            String payload,
            Integer retryCount,
            String errorMessage,
            String deadReason,
            Instant deadAt,
            OrderOutboxReplayStatus replayStatus,
            Integer replayCount,
            Instant lastReplayAt,
            String lastReplayMessage,
            Instant createdAt,
            Instant updatedAt) {
        return new OrderOutboxDeadLetter(
                id,
                outboxId == null ? null : OutboxId.of(outboxId),
                eventCode == null ? null : EventCode.of(eventCode),
                orderNo == null ? null : OrderNo.of(orderNo),
                eventType,
                businessKey,
                payload,
                retryCount,
                errorMessage,
                deadReason,
                deadAt,
                replayStatus,
                replayCount,
                lastReplayAt,
                lastReplayMessage,
                createdAt,
                updatedAt);
    }

    public static OrderOutboxDeadLetter reconstruct(
            Long id,
            OutboxId outboxId,
            EventCode eventCode,
            OrderNo orderNo,
            OrderOutboxEventType eventType,
            String businessKey,
            String payload,
            Integer retryCount,
            String errorMessage,
            String deadReason,
            Instant deadAt,
            OrderOutboxReplayStatus replayStatus,
            Integer replayCount,
            Instant lastReplayAt,
            String lastReplayMessage,
            Instant createdAt,
            Instant updatedAt) {
        return new OrderOutboxDeadLetter(
                id,
                outboxId,
                eventCode,
                orderNo,
                eventType,
                businessKey,
                payload,
                retryCount,
                errorMessage,
                deadReason,
                deadAt,
                replayStatus,
                replayCount,
                lastReplayAt,
                lastReplayMessage,
                createdAt,
                updatedAt);
    }
}
