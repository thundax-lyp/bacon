package com.github.thundax.bacon.payment.interfaces.response;

import java.time.Instant;

/**
 * 支付审计日志响应对象。
 */
public record PaymentAuditLogResponse(
        /** 所属租户主键。 */
        Long tenantId,
        /** 支付单号。 */
        String paymentNo,
        /** 操作类型。 */
        String actionType,
        /** 操作前状态。 */
        String beforeStatus,
        /** 操作后状态。 */
        String afterStatus,
        /** 操作人类型。 */
        String operatorType,
        /** 操作人主键。 */
        Long operatorId,
        /** 发生时间。 */
        Instant occurredAt) {
}
