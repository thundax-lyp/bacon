package com.github.thundax.bacon.inventory.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存审计日志。
 */
@Getter
@AllArgsConstructor
public class InventoryAuditLog {

    public static final String ACTION_RESERVE = "RESERVE";
    public static final String ACTION_RESERVE_FAILED = "RESERVE_FAILED";
    public static final String ACTION_RELEASE = "RELEASE";
    public static final String ACTION_DEDUCT = "DEDUCT";
    public static final String ACTION_AUDIT_REPLAY_SUCCEEDED = "AUDIT_REPLAY_SUCCEEDED";
    public static final String ACTION_AUDIT_REPLAY_FAILED = "AUDIT_REPLAY_FAILED";

    public static final String OPERATOR_TYPE_SYSTEM = "SYSTEM";
    public static final Long OPERATOR_ID_SYSTEM = 0L;

    /** 审计日志主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 预占单号。 */
    private String reservationNo;
    /** 操作类型。 */
    private String actionType;
    /** 操作人类型。 */
    private String operatorType;
    /** 操作人主键。 */
    private Long operatorId;
    /** 发生时间。 */
    private Instant occurredAt;
}
