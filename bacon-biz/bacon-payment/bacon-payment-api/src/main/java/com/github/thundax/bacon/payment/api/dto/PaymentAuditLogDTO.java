package com.github.thundax.bacon.payment.api.dto;

import java.time.Instant;

/**
 * 支付审计日志传输对象。
 */
public record PaymentAuditLogDTO(
        /** 所属租户主键。 */
        String tenantId,
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
        String operatorId,
        /** 发生时间。 */
        Instant occurredAt) {
}
