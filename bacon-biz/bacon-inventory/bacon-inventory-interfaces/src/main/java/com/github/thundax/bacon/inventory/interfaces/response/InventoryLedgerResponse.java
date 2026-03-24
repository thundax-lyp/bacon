package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryLedgerDTO;
import java.time.Instant;

public record InventoryLedgerResponse(Long id, Long tenantId, String orderNo, String reservationNo, Long skuId,
                                      Long warehouseId, String ledgerType, Integer quantity, Instant occurredAt) {

    public static InventoryLedgerResponse from(InventoryLedgerDTO dto) {
        return new InventoryLedgerResponse(dto.getId(), dto.getTenantId(), dto.getOrderNo(), dto.getReservationNo(),
                dto.getSkuId(), dto.getWarehouseId(), dto.getLedgerType(), dto.getQuantity(), dto.getOccurredAt());
    }
}
