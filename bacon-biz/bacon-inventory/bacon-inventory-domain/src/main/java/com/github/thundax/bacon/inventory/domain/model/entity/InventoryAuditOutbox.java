package com.github.thundax.bacon.inventory.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计出站事件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditOutbox {

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_RETRYING = "RETRYING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_DEAD = "DEAD";

    /** 出站事件主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 预占单号。 */
    private String reservationNo;
    /** 操作类型。 */
    private String actionType;
    /** 操作人类型。 */
    private String operatorType;
    /** 操作人主键。 */
    private Long operatorId;
    /** 发生时间。 */
    private Instant occurredAt;
    /** 错误信息。 */
    private String errorMessage;
    /** 当前状态。 */
    private String status;
    /** 重试次数。 */
    private Integer retryCount;
    /** 下次重试时间。 */
    private Instant nextRetryAt;
    /** 当前处理节点标识。 */
    private String processingOwner;
    /** 租约到期时间。 */
    private Instant leaseUntil;
    /** 领取处理时间。 */
    private Instant claimedAt;
    /** 死信原因。 */
    private String deadReason;
    /** 失败时间。 */
    private Instant failedAt;
    /** 最后更新时间。 */
    private Instant updatedAt;
}
