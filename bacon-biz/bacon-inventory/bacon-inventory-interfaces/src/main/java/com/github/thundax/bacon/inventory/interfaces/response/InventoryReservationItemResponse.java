package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.application.dto.InventoryReservationItemDTO;

/**
 * 库存预占明细响应对象。
 */
public record InventoryReservationItemResponse(
        /** 商品 SKU 主键。 */
        Long skuId,
        /** 预占数量。 */
        Integer quantity) {

    public static InventoryReservationItemResponse from(InventoryReservationItemDTO dto) {
        return new InventoryReservationItemResponse(dto.getSkuId(), dto.getQuantity());
    }
}
