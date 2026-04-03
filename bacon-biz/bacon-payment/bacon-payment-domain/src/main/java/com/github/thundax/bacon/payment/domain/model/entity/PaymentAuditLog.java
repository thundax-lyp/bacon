package com.github.thundax.bacon.payment.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditActionType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditOperatorType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.model.valueobject.PaymentNo;
import lombok.Getter;

import java.time.Instant;

/**
 * 支付操作审计日志。
 */
@Getter
public class PaymentAuditLog {

    /** 审计日志主键。 */
    private final Long id;
    /** 所属租户主键。 */
    private final TenantId tenantId;
    /** 支付单号。 */
    private final PaymentNo paymentNo;
    /** 操作类型。 */
    private final PaymentAuditActionType actionType;
    /** 操作前状态。 */
    private final PaymentStatus beforeStatus;
    /** 操作后状态。 */
    private final PaymentStatus afterStatus;
    /** 操作人类型。 */
    private final PaymentAuditOperatorType operatorType;
    /** 操作人主键。 */
    private final String operatorId;
    /** 发生时间。 */
    private final Instant occurredAt;

    public PaymentAuditLog(Long id, TenantId tenantId, PaymentNo paymentNo, PaymentAuditActionType actionType,
                           PaymentStatus beforeStatus, PaymentStatus afterStatus, PaymentAuditOperatorType operatorType,
                           String operatorId, Instant occurredAt) {
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
