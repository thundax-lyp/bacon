package com.github.thundax.bacon.order.domain.model.entity;

import java.time.Instant;

/**
 * 订单库存快照。
 */
public record OrderInventorySnapshot(
        /** 快照主键。 */
        Long id,
        /** 所属租户主键。 */
        Long tenantId,
        /** 订单主键。 */
        Long orderId,
        /** 库存预占单号。 */
        String reservationNo,
        /** 库存状态。 */
        String inventoryStatus,
        /** 仓库主键。 */
        Long warehouseId,
        /** 失败原因。 */
        String failureReason,
        /** 最后更新时间。 */
        Instant updatedAt
) {
}
