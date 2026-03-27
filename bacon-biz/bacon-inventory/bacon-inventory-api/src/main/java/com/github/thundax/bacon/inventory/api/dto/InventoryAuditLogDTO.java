package com.github.thundax.bacon.inventory.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计日志传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditLogDTO {

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
