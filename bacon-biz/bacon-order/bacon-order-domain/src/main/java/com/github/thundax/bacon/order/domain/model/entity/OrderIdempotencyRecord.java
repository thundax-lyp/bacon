package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 订单幂等处理记录。
 */
@Getter
@Setter
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
        return new OrderIdempotencyRecord(key, null, null, null, processingOwner, leaseUntil, claimedAt, null, null);
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
        return new OrderIdempotencyRecord(
                key, status, attemptCount, lastError, processingOwner, leaseUntil, claimedAt, createdAt, updatedAt);
    }

    public OrderNo getOrderNo() {
        return key == null ? null : key.orderNo();
    }

    public String getEventType() {
        return key == null ? null : key.eventType();
    }
}
