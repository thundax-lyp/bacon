package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;

public record InventoryReservationItemResponse(Long skuId, Integer quantity) {

    public static InventoryReservationItemResponse from(InventoryReservationItemDTO dto) {
        return new InventoryReservationItemResponse(dto.getSkuId(), dto.getQuantity());
    }
}
