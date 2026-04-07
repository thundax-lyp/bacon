package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存审计日志。
 */
@Getter
@AllArgsConstructor
public class InventoryAuditLog {

    public static final String ACTION_RESERVE = InventoryAuditActionType.RESERVE.name();
    public static final String ACTION_RESERVE_FAILED = InventoryAuditActionType.RESERVE_FAILED.name();
    public static final String ACTION_RELEASE = InventoryAuditActionType.RELEASE.name();
    public static final String ACTION_DEDUCT = InventoryAuditActionType.DEDUCT.name();
    public static final String ACTION_AUDIT_REPLAY_SUCCEEDED = InventoryAuditActionType.AUDIT_REPLAY_SUCCEEDED.name();
    public static final String ACTION_AUDIT_REPLAY_FAILED = InventoryAuditActionType.AUDIT_REPLAY_FAILED.name();

    public static final String OPERATOR_TYPE_SYSTEM = InventoryAuditOperatorType.SYSTEM.name();
    public static final Long OPERATOR_ID_SYSTEM = 0L;

    /** 审计日志主键。 */
    private Long id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 操作类型。 */
    private InventoryAuditActionType actionType;
    /** 操作人类型。 */
    private InventoryAuditOperatorType operatorType;
    /** 操作人主键。 */
    private Long operatorId;
    /** 发生时间。 */
    private Instant occurredAt;

    public InventoryAuditLog(Long id, Long tenantId, String orderNo, String reservationNo, String actionType,
                             String operatorType, Long operatorId, Instant occurredAt) {
        this(id,
                tenantId == null ? null : TenantId.of(tenantId),
                orderNo == null ? null : OrderNo.of(orderNo),
                reservationNo == null ? null : ReservationNo.of(reservationNo),
                actionType == null ? null : InventoryAuditActionType.fromValue(actionType),
                operatorType == null ? null : InventoryAuditOperatorType.fromValue(operatorType),
                operatorId,
                occurredAt);
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public String getOrderNoValue() {
        return orderNo == null ? null : orderNo.value();
    }

    public String getReservationNoValue() {
        return reservationNo == null ? null : reservationNo.value();
    }

    public String getActionTypeValue() {
        return actionType == null ? null : actionType.value();
    }

    public String getOperatorTypeValue() {
        return operatorType == null ? null : operatorType.value();
    }
}
