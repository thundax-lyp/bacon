package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.order.domain.exception.OrderDomainException;
import com.github.thundax.bacon.order.domain.exception.OrderErrorCode;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单幂等处理记录。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderIdempotencyRecord {

    /** 幂等业务键。 */
    private OrderIdempotencyRecordKey key;
    /** 当前处理状态。 */
    private OrderIdempotencyStatus status;
    /** 尝试次数。 */
    private Integer attemptCount;
    /** 最近一次错误信息。 */
    private String lastError;
    /** 当前处理节点标识。 */
    private String processingOwner;
    /** 租约到期时间。 */
    private Instant leaseUntil;
    /** 领取处理时间。 */
    private Instant claimedAt;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static OrderIdempotencyRecord create(
            OrderIdempotencyRecordKey key, String processingOwner, Instant leaseUntil, Instant claimedAt) {
        Instant createdAt = claimedAt == null ? Instant.now() : claimedAt;
        return new OrderIdempotencyRecord(
                key,
                OrderIdempotencyStatus.READY,
                0,
                null,
                processingOwner,
                leaseUntil,
                claimedAt,
                createdAt,
                createdAt);
    }

    public static OrderIdempotencyRecord reconstruct(
            OrderIdempotencyRecordKey key,
            OrderIdempotencyStatus status,
            Integer attemptCount,
            String lastError,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant createdAt,
            Instant updatedAt) {
        Instant resolvedCreatedAt = createdAt == null ? Instant.now() : createdAt;
        return new OrderIdempotencyRecord(
                key,
                status == null ? OrderIdempotencyStatus.READY : status,
                attemptCount == null ? 0 : attemptCount,
                lastError,
                processingOwner,
                leaseUntil,
                claimedAt,
                resolvedCreatedAt,
                updatedAt == null ? resolvedCreatedAt : updatedAt);
    }

    public boolean isSuccess() {
        return this.status == OrderIdempotencyStatus.SUCCESS;
    }

    public boolean isFailed() {
        return this.status == OrderIdempotencyStatus.FAILED;
    }

    public boolean isProcessing() {
        return this.status == OrderIdempotencyStatus.PROCESSING;
    }

    public boolean isLeaseExpired(Instant now) {
        return this.leaseUntil == null || !this.leaseUntil.isAfter(now);
    }

    public boolean isProcessingAndLeaseExpired(Instant now) {
        return isProcessing() && isLeaseExpired(now);
    }

    public boolean isProcessingAndLeaseActive(Instant now) {
        return isProcessing() && !isLeaseExpired(now);
    }

    public void startProcessing(Instant now) {
        if (this.status != OrderIdempotencyStatus.READY) {
            throw new OrderDomainException(OrderErrorCode.INVALID_IDEMPOTENCY_STATUS, String.valueOf(this.status));
        }
        this.status = OrderIdempotencyStatus.PROCESSING;
        this.attemptCount = this.attemptCount <= 0 ? 1 : this.attemptCount;
        this.lastError = null;
        this.updatedAt = now;
    }

    public void claim(String processingOwner, Instant leaseUntil, Instant claimedAt, Instant updatedAt) {
        ensureStatus(OrderIdempotencyStatus.PROCESSING);
        this.processingOwner = processingOwner;
        this.leaseUntil = leaseUntil;
        this.claimedAt = claimedAt;
        this.updatedAt = updatedAt;
    }

    public void markSuccess(Instant updatedAt) {
        ensureStatus(OrderIdempotencyStatus.PROCESSING);
        this.status = OrderIdempotencyStatus.SUCCESS;
        this.lastError = null;
        this.processingOwner = null;
        this.leaseUntil = null;
        this.claimedAt = null;
        this.updatedAt = updatedAt;
    }

    public void markFailed(String lastError, Instant updatedAt) {
        ensureStatus(OrderIdempotencyStatus.PROCESSING);
        this.status = OrderIdempotencyStatus.FAILED;
        this.lastError = lastError;
        this.processingOwner = null;
        this.leaseUntil = null;
        this.claimedAt = null;
        this.updatedAt = updatedAt;
    }

    public void expire(String lastError, Instant now) {
        if (!isProcessingAndLeaseExpired(now)) {
            throw new OrderDomainException(OrderErrorCode.INVALID_IDEMPOTENCY_STATUS, String.valueOf(this.status));
        }
        this.status = OrderIdempotencyStatus.FAILED;
        this.lastError = lastError;
        this.processingOwner = null;
        this.leaseUntil = null;
        this.claimedAt = null;
        this.updatedAt = now;
    }

    public void recover(String processingOwner, Instant leaseUntil, Instant claimedAt, Instant updatedAt) {
        ensureStatus(OrderIdempotencyStatus.FAILED);
        this.status = OrderIdempotencyStatus.PROCESSING;
        this.attemptCount = (this.attemptCount == null ? 0 : this.attemptCount) + 1;
        this.lastError = null;
        this.processingOwner = processingOwner;
        this.leaseUntil = leaseUntil;
        this.claimedAt = claimedAt;
        this.updatedAt = updatedAt;
    }

    private void ensureStatus(OrderIdempotencyStatus expectedStatus) {
        if (this.status == expectedStatus) {
            return;
        }
        throw new OrderDomainException(OrderErrorCode.INVALID_IDEMPOTENCY_STATUS, String.valueOf(this.status));
    }
}
