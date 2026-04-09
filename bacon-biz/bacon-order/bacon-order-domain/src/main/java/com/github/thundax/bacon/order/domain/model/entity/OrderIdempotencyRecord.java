package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单幂等处理记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    public OrderIdempotencyRecord(Long tenantId, String orderNo, String eventType, OrderIdempotencyStatus status,
                                  Integer attemptCount, String lastError, String processingOwner,
                                  Instant leaseUntil, Instant claimedAt, Instant createdAt, Instant updatedAt) {
        this(OrderIdempotencyRecordKey.of(
                        tenantId == null ? null : TenantId.of(tenantId),
                        orderNo == null ? null : OrderNo.of(orderNo),
                        eventType),
                status, attemptCount, lastError, processingOwner, leaseUntil, claimedAt, createdAt, updatedAt);
    }

    public Long getTenantIdValue() {
        return key == null ? null : key.tenantId().value();
    }

    public String getOrderNoValue() {
        return key == null ? null : key.orderNo().value();
    }

    public String getEventTypeValue() {
        return key == null ? null : key.eventType();
    }

    public String getStatusValue() {
        return status == null ? null : status.value();
    }

    public TenantId getTenantId() {
        return key == null ? null : key.tenantId();
    }

    public OrderNo getOrderNo() {
        return key == null ? null : key.orderNo();
    }

    public String getEventType() {
        return key == null ? null : key.eventType();
    }
}
