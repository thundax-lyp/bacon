package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.enums.OperatorType;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单操作审计日志。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderAuditLog {

    /** 审计日志主键。 */
    private Long id;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 操作类型。 */
    private OrderAuditActionType actionType;
    /** 操作前状态。 */
    private OrderStatus beforeStatus;
    /** 操作后状态。 */
    private OrderStatus afterStatus;
    /** 操作人类型。 */
    private OperatorType operatorType;
    /** 操作人标识。 */
    private String operatorId;
    /** 发生时间。 */
    private Instant occurredAt;

    public static OrderAuditLog create(
            OrderNo orderNo,
            OrderAuditActionType actionType,
            OrderStatus beforeStatus,
            OrderStatus afterStatus,
            OperatorType operatorType,
            String operatorId,
            Instant occurredAt) {
        return new OrderAuditLog(
                null, orderNo, actionType, beforeStatus, afterStatus, operatorType, operatorId, occurredAt);
    }

    public static OrderAuditLog reconstruct(
            Long id,
            OrderNo orderNo,
            OrderAuditActionType actionType,
            OrderStatus beforeStatus,
            OrderStatus afterStatus,
            OperatorType operatorType,
            String operatorId,
            Instant occurredAt) {
        return new OrderAuditLog(
                id, orderNo, actionType, beforeStatus, afterStatus, operatorType, operatorId, occurredAt);
    }
}
