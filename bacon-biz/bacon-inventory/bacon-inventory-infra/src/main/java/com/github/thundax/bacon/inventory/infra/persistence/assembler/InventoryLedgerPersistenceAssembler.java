package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryLedgerDO;

public final class InventoryLedgerPersistenceAssembler {

    private InventoryLedgerPersistenceAssembler() {}

    public static InventoryLedger toDomain(InventoryLedgerDO dataObject) {
        return new InventoryLedger(
                dataObject.getId(),
                dataObject.getTenantId() == null ? null : TenantId.of(dataObject.getTenantId()),
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
                ledger.getTenantId() == null ? null : ledger.getTenantId().value(),
                ledger.getOrderNoValue(),
                ledger.getReservationNoValue(),
                ledger.getSkuId() == null ? null : ledger.getSkuId().value(),
                ledger.getWarehouseCodeValue(),
                ledger.getLedgerTypeValue(),
                ledger.getQuantity(),
                ledger.getOccurredAt());
    }
}
