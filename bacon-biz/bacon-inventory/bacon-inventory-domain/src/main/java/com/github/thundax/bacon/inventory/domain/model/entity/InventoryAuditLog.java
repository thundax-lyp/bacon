package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存审计日志。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryAuditLog {

    public static final OperatorId OPERATOR_ID_SYSTEM = OperatorId.of("0");

    /** 审计日志主键，使用雪花 ID 保持系统内审计事件标识生成方式一致。 */
    private Long id;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 操作类型。 */
    private InventoryAuditActionType actionType;
    /** 操作人类型。 */
    private InventoryAuditOperatorType operatorType;
    /** 操作人主键。 */
    private OperatorId operatorId;
    /** 发生时间。 */
    private Instant occurredAt;

    public static InventoryAuditLog create(
            Long id,
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryAuditActionType actionType,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant occurredAt) {
        return new InventoryAuditLog(id, orderNo, reservationNo, actionType, operatorType, operatorId, occurredAt);
    }

    public static InventoryAuditLog reconstruct(
            Long id,
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryAuditActionType actionType,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant occurredAt) {
        return new InventoryAuditLog(id, orderNo, reservationNo, actionType, operatorType, operatorId, occurredAt);
    }
}
