package com.github.thundax.bacon.payment.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import lombok.Getter;

import java.time.Instant;

/**
 * 支付操作审计日志。
 */
@Getter
public class PaymentAuditLog {

    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_CALLBACK_PAID = "CALLBACK_PAID";
    public static final String ACTION_CALLBACK_FAILED = "CALLBACK_FAILED";
    public static final String ACTION_CLOSE = "CLOSE";
    public static final String OPERATOR_SYSTEM = "SYSTEM";
    public static final String OPERATOR_CHANNEL = "CHANNEL";

    /** 审计日志主键。 */
    private final Long id;
    /** 所属租户主键。 */
    private final TenantId tenantId;
    /** 支付单号。 */
    private final String paymentNo;
    /** 操作类型。 */
    private final String actionType;
    /** 操作前状态。 */
    private final String beforeStatus;
    /** 操作后状态。 */
    private final String afterStatus;
    /** 操作人类型。 */
    private final String operatorType;
    /** 操作人主键。 */
    private final Long operatorId;
    /** 发生时间。 */
    private final Instant occurredAt;

    public PaymentAuditLog(Long id, TenantId tenantId, String paymentNo, String actionType, String beforeStatus,
                           String afterStatus, String operatorType, Long operatorId, Instant occurredAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.paymentNo = paymentNo;
        this.actionType = actionType;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.operatorType = operatorType;
        this.operatorId = operatorId;
        this.occurredAt = occurredAt;
    }
}
