package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单库存快照。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderInventorySnapshot {

    /** 订单号。 */
    private OrderNo orderNo;
    /** 库存预占单号。 */
    private ReservationNo reservationNo;
    /** 库存状态。 */
    private InventoryStatus inventoryStatus;
    /** 仓库业务编码。 */
    private WarehouseCode warehouseCode;
    /** 失败原因。 */
    private String failureReason;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static OrderInventorySnapshot create(
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryStatus inventoryStatus,
            WarehouseCode warehouseCode,
            String failureReason,
            Instant updatedAt) {
        return new OrderInventorySnapshot(orderNo, reservationNo, inventoryStatus, warehouseCode, failureReason, updatedAt);
    }

    public static OrderInventorySnapshot reconstruct(
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryStatus inventoryStatus,
            WarehouseCode warehouseCode,
            String failureReason,
            Instant updatedAt) {
        return new OrderInventorySnapshot(orderNo, reservationNo, inventoryStatus, warehouseCode, failureReason, updatedAt);
    }
}
