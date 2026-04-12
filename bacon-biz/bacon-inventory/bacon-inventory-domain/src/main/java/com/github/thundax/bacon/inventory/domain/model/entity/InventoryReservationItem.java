package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存预占明细领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryReservationItem {

    /** 明细主键。 */
    private Long id;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 商品 SKU 主键。 */
    private SkuId skuId;
    /** 预占数量。 */
    private Integer quantity;

    public static InventoryReservationItem create(Long id, ReservationNo reservationNo, SkuId skuId, Integer quantity) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(reservationNo, "reservationNo must not be null");
        Objects.requireNonNull(skuId, "skuId must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        return new InventoryReservationItem(id, reservationNo, skuId, quantity);
    }

    public static InventoryReservationItem reconstruct(
            Long id, ReservationNo reservationNo, SkuId skuId, Integer quantity) {
        return new InventoryReservationItem(id, reservationNo, skuId, quantity);
    }
}
