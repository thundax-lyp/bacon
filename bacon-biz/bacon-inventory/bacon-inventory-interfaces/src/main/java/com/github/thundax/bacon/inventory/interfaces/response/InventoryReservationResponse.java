package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import java.time.Instant;
import java.util.List;

/**
 * 库存预占响应对象。
 */
public record InventoryReservationResponse(
        /** 所属租户主键。 */
        Long tenantId,
        /** 订单号。 */
        String orderNo,
        /** 预占单号。 */
        String reservationNo,
        /** 预占状态。 */
        String reservationStatus,
        /** 仓库业务编号。 */
        String warehouseNo,
        /** 预占明细列表。 */
        List<InventoryReservationItemResponse> items,
        /** 失败原因。 */
        String failureReason,
        /** 释放原因。 */
        String releaseReason,
        /** 创建时间。 */
        Instant createdAt,
        /** 释放时间。 */
        Instant releasedAt,
        /** 扣减时间。 */
        Instant deductedAt) {

    public static InventoryReservationResponse from(InventoryReservationDTO dto) {
        return new InventoryReservationResponse(dto.getTenantId(), dto.getOrderNo(), dto.getReservationNo(),
                dto.getReservationStatus(), dto.getWarehouseNo(), dto.getItems().stream()
                .map(InventoryReservationItemResponse::from)
                .toList(), dto.getFailureReason(), dto.getReleaseReason(), dto.getCreatedAt(), dto.getReleasedAt(),
                dto.getDeductedAt());
    }
}
