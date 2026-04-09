package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryLedgerDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.WarehouseNo;

public final class InventoryLedgerAssembler {

    private InventoryLedgerAssembler() {
    }

    public static InventoryLedgerDTO toDto(InventoryLedger ledger) {
        return new InventoryLedgerDTO(
                ledger.getId(),
                ledger.getTenantId() == null ? null : ledger.getTenantId().value(),
                ledger.getOrderNoValue(),
                ledger.getReservationNoValue(),
                ledger.getSkuId() == null ? null : ledger.getSkuId().value(),
                ledger.getWarehouseNoValue(),
                ledger.getLedgerTypeValue(),
                ledger.getQuantity(),
                ledger.getOccurredAt());
    }

    public static InventoryLedger toDomain(InventoryLedgerDTO dto) {
        return new InventoryLedger(
                dto.getId(),
                dto.getTenantId() == null ? null : TenantId.of(dto.getTenantId()),
                dto.getOrderNo() == null ? null : OrderNo.of(dto.getOrderNo()),
                dto.getReservationNo() == null ? null : ReservationNo.of(dto.getReservationNo()),
                dto.getSkuId() == null ? null : SkuId.of(dto.getSkuId()),
                dto.getWarehouseNo() == null ? null : WarehouseNo.of(dto.getWarehouseNo()),
                dto.getLedgerType() == null ? null : InventoryLedgerType.from(dto.getLedgerType()),
                dto.getQuantity(),
                dto.getOccurredAt());
    }
}
