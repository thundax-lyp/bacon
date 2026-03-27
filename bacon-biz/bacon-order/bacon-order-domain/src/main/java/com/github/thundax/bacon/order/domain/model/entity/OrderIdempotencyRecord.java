package com.github.thundax.bacon.order.domain.model.entity;

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

    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    /** 幂等记录主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 支付单号。 */
    private String paymentNo;
    /** 事件类型。 */
    private String eventType;
    /** 当前处理状态。 */
    private String status;
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
}
