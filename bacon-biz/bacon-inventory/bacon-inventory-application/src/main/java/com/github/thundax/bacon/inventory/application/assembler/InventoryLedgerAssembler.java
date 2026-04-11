package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.inventory.api.dto.InventoryLedgerDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;

public final class InventoryLedgerAssembler {

    private InventoryLedgerAssembler() {}

    public static InventoryLedgerDTO toDto(Long tenantId, InventoryLedger ledger) {
        return new InventoryLedgerDTO(
                ledger.getId(),
                tenantId,
                ledger.getOrderNo() == null ? null : ledger.getOrderNo().value(),
                ledger.getReservationNo() == null ? null : ledger.getReservationNo().value(),
                ledger.getSkuId() == null ? null : ledger.getSkuId().value(),
                ledger.getWarehouseCode() == null ? null : ledger.getWarehouseCode().value(),
                ledger.getLedgerType() == null ? null : ledger.getLedgerType().value(),
                ledger.getQuantity(),
                ledger.getOccurredAt());
    }

    public static InventoryLedger toDomain(InventoryLedgerDTO dto) {
        return new InventoryLedger(
                dto.getId(),
                dto.getOrderNo() == null ? null : OrderNo.of(dto.getOrderNo()),
                dto.getReservationNo() == null ? null : ReservationNo.of(dto.getReservationNo()),
                dto.getSkuId() == null ? null : SkuId.of(dto.getSkuId()),
                dto.getWarehouseCode() == null ? null : WarehouseCode.of(dto.getWarehouseCode()),
                dto.getLedgerType() == null ? null : InventoryLedgerType.from(dto.getLedgerType()),
                dto.getQuantity(),
                dto.getOccurredAt());
    }
}
