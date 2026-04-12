package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryLedgerDO;

public final class InventoryLedgerPersistenceAssembler {

    private InventoryLedgerPersistenceAssembler() {}

    public static InventoryLedger toDomain(InventoryLedgerDO dataObject) {
        return new InventoryLedger(
                dataObject.getId(),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getReservationNo() == null ? null : ReservationNo.of(dataObject.getReservationNo()),
                dataObject.getSkuId() == null ? null : SkuId.of(dataObject.getSkuId()),
                dataObject.getWarehouseCode() == null ? null : WarehouseCode.of(dataObject.getWarehouseCode()),
                InventoryLedgerType.from(dataObject.getLedgerType()),
                dataObject.getQuantity(),
                dataObject.getOccurredAt());
    }

    public static InventoryLedgerDO toDataObject(InventoryLedger ledger) {
        return new InventoryLedgerDO(
                ledger.getId(),
                BaconContextHolder.requireTenantId(),
                ledger.getOrderNo() == null ? null : ledger.getOrderNo().value(),
                ledger.getReservationNo() == null
                        ? null
                        : ledger.getReservationNo().value(),
                ledger.getSkuId() == null ? null : ledger.getSkuId().value(),
                ledger.getWarehouseCode() == null
                        ? null
                        : ledger.getWarehouseCode().value(),
                ledger.getLedgerType() == null ? null : ledger.getLedgerType().value(),
                ledger.getQuantity(),
                ledger.getOccurredAt());
    }
}
