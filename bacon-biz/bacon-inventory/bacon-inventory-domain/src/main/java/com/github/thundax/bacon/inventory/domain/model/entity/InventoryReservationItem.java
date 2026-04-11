package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存预占明细领域实体。
 */
@Getter
@AllArgsConstructor
public class InventoryReservationItem {

    /** 明细主键。 */
    private Long id;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 商品 SKU 主键。 */
    private SkuId skuId;
    /** 预占数量。 */
    private Integer quantity;
}
