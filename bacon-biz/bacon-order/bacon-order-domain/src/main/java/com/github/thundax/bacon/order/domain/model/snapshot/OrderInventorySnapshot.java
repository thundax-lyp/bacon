package com.github.thundax.bacon.order.domain.model.snapshot;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import java.time.Instant;

/**
 * 订单库存快照。
 */
public record OrderInventorySnapshot(
        Long id,
        OrderNo orderNo,
        ReservationNo reservationNo,
        InventoryStatus inventoryStatus,
        WarehouseCode warehouseCode,
        String failureReason,
        Instant updatedAt) {

    public static OrderInventorySnapshot create(
            Long id,
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryStatus inventoryStatus,
            WarehouseCode warehouseCode,
            String failureReason,
            Instant updatedAt) {
        return new OrderInventorySnapshot(id, orderNo, reservationNo, inventoryStatus, warehouseCode, failureReason, updatedAt);
    }

    public static OrderInventorySnapshot reconstruct(
            Long id,
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryStatus inventoryStatus,
            WarehouseCode warehouseCode,
            String failureReason,
            Instant updatedAt) {
        return new OrderInventorySnapshot(id, orderNo, reservationNo, inventoryStatus, warehouseCode, failureReason, updatedAt);
    }
}
