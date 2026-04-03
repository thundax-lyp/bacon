package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import java.time.Instant;

/**
 * 订单操作审计日志。
 */
public record OrderAuditLog(
        /** 审计日志主键。 */
        Long id,
        /** 所属租户主键。 */
        TenantId tenantId,
        /** 订单号。 */
        String orderNo,
        /** 操作类型。 */
        String actionType,
        /** 操作前状态。 */
        String beforeStatus,
        /** 操作后状态。 */
        String afterStatus,
        /** 操作人类型。 */
        String operatorType,
        /** 操作人标识。 */
        String operatorId,
        /** 发生时间。 */
        Instant occurredAt
) {
}
