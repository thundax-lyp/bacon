package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.exception.OrderDomainException;
import com.github.thundax.bacon.order.domain.exception.OrderErrorCode;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单出站事件死信记录。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderOutboxDeadLetter {

    private static final int MESSAGE_MAX_LENGTH = 512;

    /** 死信记录主键。 */
    private OrderOutboxDeadLetterId id;
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
            OrderOutboxDeadLetterId id,
            OrderOutboxEvent event,
            Integer retryCount,
            String errorMessage,
            String deadReason,
            Instant deadAt) {
        OrderOutboxEvent sourceEvent = Objects.requireNonNull(event, "outbox event must not be null");
        Instant resolvedDeadAt = deadAt == null ? Instant.now() : deadAt;
        return new OrderOutboxDeadLetter(
                id,
                sourceEvent.getId(),
                sourceEvent.getEventCode(),
                sourceEvent.getOrderNo(),
                sourceEvent.getEventType(),
                sourceEvent.getBusinessKey(),
                sourceEvent.getPayload(),
                retryCount,
                normalizeMessage(errorMessage),
                deadReason,
                resolvedDeadAt,
                OrderOutboxReplayStatus.PENDING,
                0,
                null,
                null,
                resolvedDeadAt,
                resolvedDeadAt);
    }

    public static OrderOutboxDeadLetter reconstruct(
            OrderOutboxDeadLetterId id,
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
        Instant resolvedDeadAt = deadAt == null ? Instant.now() : deadAt;
        Instant resolvedCreatedAt = createdAt == null ? resolvedDeadAt : createdAt;
        return new OrderOutboxDeadLetter(
                id,
                outboxId,
                eventCode,
                orderNo,
                eventType,
                businessKey,
                payload,
                retryCount,
                normalizeMessage(errorMessage),
                deadReason,
                resolvedDeadAt,
                replayStatus == null ? OrderOutboxReplayStatus.PENDING : replayStatus,
                replayCount == null ? 0 : replayCount,
                lastReplayAt,
                normalizeReplayMessage(lastReplayMessage),
                resolvedCreatedAt,
                updatedAt == null ? resolvedCreatedAt : updatedAt);
    }

    public void markReplaySucceeded(Instant replayedAt, String message) {
        this.replayStatus = OrderOutboxReplayStatus.SUCCESS;
        this.replayCount = increaseReplayCount();
        this.lastReplayAt = replayedAt == null ? Instant.now() : replayedAt;
        this.lastReplayMessage = normalizeReplayMessage(message);
        this.updatedAt = this.lastReplayAt;
    }

    public void markReplayFailed(Instant replayedAt, String message) {
        this.replayStatus = OrderOutboxReplayStatus.FAILED;
        this.replayCount = increaseReplayCount();
        this.lastReplayAt = replayedAt == null ? Instant.now() : replayedAt;
        this.lastReplayMessage = normalizeReplayMessage(message);
        this.updatedAt = this.lastReplayAt;
    }

    public void markReplayPending(String message, Instant updatedAt) {
        if (this.replayStatus != OrderOutboxReplayStatus.FAILED) {
            throw new OrderDomainException(
                    OrderErrorCode.INVALID_OUTBOX_DEAD_LETTER,
                    this.replayStatus == null ? null : this.replayStatus.value());
        }
        this.replayStatus = OrderOutboxReplayStatus.PENDING;
        this.lastReplayMessage = normalizeReplayMessage(message);
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }

    public boolean isReplaySucceeded() {
        return this.replayStatus == OrderOutboxReplayStatus.SUCCESS;
    }

    public boolean isReplayFailed() {
        return this.replayStatus == OrderOutboxReplayStatus.FAILED;
    }

    public OrderOutboxEvent rebuildEvent() {
        return OrderOutboxEvent.reconstruct(
                this.outboxId,
                this.eventCode,
                this.orderNo,
                this.eventType,
                this.businessKey,
                this.payload,
                OrderOutboxStatus.DEAD,
                this.retryCount,
                null,
                null,
                null,
                null,
                this.errorMessage,
                this.deadReason,
                this.createdAt,
                this.updatedAt);
    }

    private static String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }
        return message.length() <= MESSAGE_MAX_LENGTH ? message : message.substring(0, MESSAGE_MAX_LENGTH);
    }

    private static String normalizeReplayMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        return message.length() <= MESSAGE_MAX_LENGTH ? message : message.substring(0, MESSAGE_MAX_LENGTH);
    }

    private int increaseReplayCount() {
        if (this.replayCount == null || this.replayCount < 0) {
            throw new OrderDomainException(OrderErrorCode.INVALID_OUTBOX_DEAD_LETTER, "replayCount");
        }
        return this.replayCount + 1;
    }
}
