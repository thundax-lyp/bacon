package com.github.thundax.bacon.inventory.interfaces.assembler;

import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationItemFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockListFacadeResponse;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.result.InventoryReservationResult;
import java.util.List;

public final class InventoryReservationResponseAssembler {

    private InventoryReservationResponseAssembler() {}

    public static InventoryReservationFacadeResponse fromResult(InventoryReservationResult result) {
        return new InventoryReservationFacadeResponse(
                result.getOrderNo(),
                result.getReservationNo(),
                result.getReservationStatus(),
                result.getInventoryStatus(),
                result.getWarehouseCode(),
                null,
                result.getFailureReason(),
                result.getReleaseReason(),
                null,
                result.getReleasedAt(),
                result.getDeductedAt());
    }

    public static InventoryReservationFacadeResponse fromDto(InventoryReservationDTO dto) {
        return new InventoryReservationFacadeResponse(
                dto.getOrderNo(),
                dto.getReservationNo(),
                dto.getReservationStatus(),
                dto.getReservationStatus(),
                dto.getWarehouseCode(),
                dto.getItems() == null ? List.of() : dto.getItems().stream()
                        .map(InventoryReservationResponseAssembler::toItemResponse)
                        .toList(),
                dto.getFailureReason(),
                dto.getReleaseReason(),
                dto.getCreatedAt(),
                dto.getReleasedAt(),
                dto.getDeductedAt());
    }

    public static InventoryStockFacadeResponse fromStockDto(InventoryStockDTO dto) {
        return new InventoryStockFacadeResponse(
                dto.getSkuId(),
                dto.getWarehouseCode(),
                dto.getOnHandQuantity(),
                dto.getReservedQuantity(),
                dto.getAvailableQuantity(),
                dto.getStatus(),
                dto.getUpdatedAt());
    }

    public static InventoryStockListFacadeResponse fromStockDtos(List<InventoryStockDTO> dtos) {
        return new InventoryStockListFacadeResponse(
                dtos == null ? List.of() : dtos.stream().map(InventoryReservationResponseAssembler::fromStockDto).toList());
    }

    private static InventoryReservationItemFacadeResponse toItemResponse(InventoryReservationItemDTO dto) {
        return new InventoryReservationItemFacadeResponse(dto.getSkuId(), dto.getQuantity());
    }
}
