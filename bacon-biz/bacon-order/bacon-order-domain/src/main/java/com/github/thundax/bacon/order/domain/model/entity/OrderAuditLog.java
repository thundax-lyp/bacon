package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.enums.OperatorType;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
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
        OrderNo orderNo,
        /** 操作类型。 */
        OrderAuditActionType actionType,
        /** 操作前状态。 */
        OrderStatus beforeStatus,
        /** 操作后状态。 */
        OrderStatus afterStatus,
        /** 操作人类型。 */
        OperatorType operatorType,
        /** 操作人标识。 */
        String operatorId,
        /** 发生时间。 */
        Instant occurredAt) {}
