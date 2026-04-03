package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
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

    public static final InventoryAuditOutboxStatus STATUS_NEW = InventoryAuditOutboxStatus.NEW;
    public static final InventoryAuditOutboxStatus STATUS_RETRYING = InventoryAuditOutboxStatus.RETRYING;
    public static final InventoryAuditOutboxStatus STATUS_PROCESSING = InventoryAuditOutboxStatus.PROCESSING;
    public static final InventoryAuditOutboxStatus STATUS_DEAD = InventoryAuditOutboxStatus.DEAD;

    /** 出站事件主键。 */
    private Long id;
    /** 所属租户主键。 */
    private String tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 预占单号。 */
    private String reservationNo;
    /** 操作类型。 */
    private String actionType;
    /** 操作人类型。 */
    private String operatorType;
    /** 操作人主键。 */
    private String operatorId;
    /** 发生时间。 */
    private Instant occurredAt;
    /** 错误信息。 */
    private String errorMessage;
    /** 当前状态。 */
    private InventoryAuditOutboxStatus status;
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

    public InventoryAuditOutbox(Long id, Long tenantId, String orderNo, String reservationNo, String actionType,
                                String operatorType, Long operatorId, Instant occurredAt, String errorMessage,
                                String status, Integer retryCount, Instant nextRetryAt, String processingOwner,
                                Instant leaseUntil, Instant claimedAt, String deadReason, Instant failedAt,
                                Instant updatedAt) {
        this(id, tenantId == null ? null : String.valueOf(tenantId), orderNo, reservationNo, actionType, operatorType,
                operatorId == null ? null : String.valueOf(operatorId), occurredAt, errorMessage,
                status == null ? null : InventoryAuditOutboxStatus.fromValue(status), retryCount, nextRetryAt,
                processingOwner, leaseUntil, claimedAt, deadReason, failedAt, updatedAt);
    }

    public InventoryAuditOutbox(Long id, Long tenantId, String orderNo, String reservationNo, String actionType,
                                String operatorType, Long operatorId, Instant occurredAt, String errorMessage,
                                InventoryAuditOutboxStatus status, Integer retryCount, Instant nextRetryAt,
                                String processingOwner, Instant leaseUntil, Instant claimedAt, String deadReason,
                                Instant failedAt, Instant updatedAt) {
        this(id, tenantId == null ? null : String.valueOf(tenantId), orderNo, reservationNo, actionType, operatorType,
                operatorId == null ? null : String.valueOf(operatorId), occurredAt, errorMessage, status, retryCount,
                nextRetryAt, processingOwner, leaseUntil, claimedAt, deadReason, failedAt, updatedAt);
    }
}
