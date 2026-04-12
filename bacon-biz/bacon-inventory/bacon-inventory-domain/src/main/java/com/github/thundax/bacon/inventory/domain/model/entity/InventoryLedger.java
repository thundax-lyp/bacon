package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存流水领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryLedger {

    /** 流水主键。 */
    private Long id;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 商品 SKU 主键。 */
    private SkuId skuId;
    /** 仓库业务编码。 */
    private WarehouseCode warehouseCode;
    /** 流水类型。 */
    private InventoryLedgerType ledgerType;
    /** 变更数量。 */
    private Integer quantity;
    /** 发生时间。 */
    private Instant occurredAt;

    public static InventoryLedger create(
            Long id,
            OrderNo orderNo,
            ReservationNo reservationNo,
            SkuId skuId,
            WarehouseCode warehouseCode,
            InventoryLedgerType ledgerType,
            Integer quantity,
            Instant occurredAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(orderNo, "orderNo must not be null");
        Objects.requireNonNull(reservationNo, "reservationNo must not be null");
        Objects.requireNonNull(skuId, "skuId must not be null");
        Objects.requireNonNull(warehouseCode, "warehouseCode must not be null");
        Objects.requireNonNull(ledgerType, "ledgerType must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        return new InventoryLedger(id, orderNo, reservationNo, skuId, warehouseCode, ledgerType, quantity, occurredAt);
    }

    public static InventoryLedger reconstruct(
            Long id,
            OrderNo orderNo,
            ReservationNo reservationNo,
            SkuId skuId,
            WarehouseCode warehouseCode,
            InventoryLedgerType ledgerType,
            Integer quantity,
            Instant occurredAt) {
        return new InventoryLedger(id, orderNo, reservationNo, skuId, warehouseCode, ledgerType, quantity, occurredAt);
    }
}
