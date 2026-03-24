package com.github.thundax.bacon.inventory.interfaces.assembler;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryReservationItemResponse;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryReservationResponse;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryStockResponse;

public final class InventoryAssembler {

    private InventoryAssembler() {
    }

    public static InventoryStockResponse toStockResponse(InventoryStockDTO dto) {
        return new InventoryStockResponse(dto.getTenantId(), dto.getSkuId(), dto.getWarehouseId(),
                dto.getOnHandQuantity(), dto.getReservedQuantity(), dto.getAvailableQuantity(), dto.getStatus(),
                dto.getUpdatedAt());
    }

    public static InventoryReservationResponse toReservationResponse(InventoryReservationDTO dto) {
        return new InventoryReservationResponse(dto.getTenantId(), dto.getOrderNo(), dto.getReservationNo(),
                dto.getReservationStatus(), dto.getWarehouseId(), dto.getItems().stream()
                .map(InventoryAssembler::toReservationItemResponse)
                .toList(), dto.getFailureReason(), dto.getReleaseReason(), dto.getCreatedAt(), dto.getReleasedAt(),
                dto.getDeductedAt());
    }

    public static InventoryReservationItemResponse toReservationItemResponse(InventoryReservationItemDTO dto) {
        return new InventoryReservationItemResponse(dto.getSkuId(), dto.getQuantity());
    }
}
