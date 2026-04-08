package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryLedgerDO;

public final class InventoryLedgerPersistenceAssembler {

    private InventoryLedgerPersistenceAssembler() {
    }

    public static InventoryLedger toDomain(InventoryLedgerDO dataObject) {
        return new InventoryLedger(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getReservationNo(), dataObject.getSkuId(),
                dataObject.getWarehouseNo(),
                InventoryLedgerType.from(dataObject.getLedgerType()), dataObject.getQuantity(),
                dataObject.getOccurredAt());
    }

    public static InventoryLedgerDO toDataObject(InventoryLedger ledger) {
        return new InventoryLedgerDO(ledger.getId(), ledger.getTenantId() == null ? null : ledger.getTenantId().value(), ledger.getOrderNoValue(),
                ledger.getReservationNoValue(), ledger.getSkuId() == null ? null : ledger.getSkuId().value(),
                ledger.getWarehouseNoValue(),
                ledger.getLedgerTypeValue(),
                ledger.getQuantity(), ledger.getOccurredAt());
    }
}
