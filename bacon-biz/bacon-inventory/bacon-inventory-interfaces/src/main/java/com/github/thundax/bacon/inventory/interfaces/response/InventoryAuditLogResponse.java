package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.application.dto.InventoryAuditLogDTO;
import java.time.Instant;

/**
 * 库存审计日志响应对象。
 */
public record InventoryAuditLogResponse(
        /** 审计日志主键。 */
        Long id,
        /** 订单号。 */
        String orderNo,
        /** 预占单号。 */
        String reservationNo,
        /** 操作类型。 */
        String actionType,
        /** 操作人类型。 */
        String operatorType,
        /** 操作人主键。 */
        Long operatorId,
        /** 发生时间。 */
        Instant occurredAt) {

    public static InventoryAuditLogResponse from(InventoryAuditLogDTO dto) {
        return new InventoryAuditLogResponse(
                dto.getId(),
                dto.getOrderNo(),
                dto.getReservationNo(),
                dto.getActionType(),
                dto.getOperatorType(),
                dto.getOperatorId(),
                dto.getOccurredAt());
    }
}
