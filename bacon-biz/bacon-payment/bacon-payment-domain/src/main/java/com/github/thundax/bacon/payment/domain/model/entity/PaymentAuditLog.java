package com.github.thundax.bacon.payment.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditActionType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditOperatorType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 支付操作审计日志。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentAuditLog {

    /** 审计日志主键。 */
    private Long id;
    /** 支付单号。 */
    private PaymentNo paymentNo;
    /** 操作类型。 */
    private PaymentAuditActionType actionType;
    /** 操作前状态。 */
    private PaymentStatus beforeStatus;
    /** 操作后状态。 */
    private PaymentStatus afterStatus;
    /** 操作人类型。 */
    private PaymentAuditOperatorType operatorType;
    /** 操作人主键。 */
    private String operatorId;
    /** 发生时间。 */
    private Instant occurredAt;

    public static PaymentAuditLog create(
            Long id,
            PaymentNo paymentNo,
            PaymentAuditActionType actionType,
            PaymentStatus beforeStatus,
            PaymentStatus afterStatus,
            PaymentAuditOperatorType operatorType,
            String operatorId,
            Instant occurredAt) {
        return new PaymentAuditLog(
                id, paymentNo, actionType, beforeStatus, afterStatus, operatorType, operatorId, occurredAt);
    }

    public static PaymentAuditLog reconstruct(
            Long id,
            PaymentNo paymentNo,
            PaymentAuditActionType actionType,
            PaymentStatus beforeStatus,
            PaymentStatus afterStatus,
            PaymentAuditOperatorType operatorType,
            String operatorId,
            Instant occurredAt) {
        return new PaymentAuditLog(
                id, paymentNo, actionType, beforeStatus, afterStatus, operatorType, operatorId, occurredAt);
    }
}
