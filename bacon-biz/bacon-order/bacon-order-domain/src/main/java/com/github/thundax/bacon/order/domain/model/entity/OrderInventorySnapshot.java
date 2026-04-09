package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import java.time.Instant;

/**
 * 订单库存快照。
 */
public record OrderInventorySnapshot(
        /** 所属租户主键。 */
        TenantId tenantId,
        /** 订单号。 */
        OrderNo orderNo,
        /** 库存预占单号。 */
        ReservationNo reservationNo,
        /** 库存状态。 */
        InventoryStatus inventoryStatus,
        /** 仓库业务编码。 */
        WarehouseCode warehouseCode,
        /** 失败原因。 */
        String failureReason,
        /** 最后更新时间。 */
        Instant updatedAt
) {

    public Long tenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public String orderNoValue() {
        return orderNo == null ? null : orderNo.value();
    }

    public String reservationNoValue() {
        return reservationNo == null ? null : reservationNo.value();
    }

    public String inventoryStatusValue() {
        return inventoryStatus == null ? null : inventoryStatus.value();
    }

    public String warehouseCodeValue() {
        return warehouseCode == null ? null : warehouseCode.value();
    }
}
