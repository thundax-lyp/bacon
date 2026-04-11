package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.valueobject.Version;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservedQuantity;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryDO;

public final class InventoryPersistenceAssembler {

    private InventoryPersistenceAssembler() {}

    public static Inventory toDomain(InventoryDO dataObject) {
        return Inventory.reconstruct(
                dataObject.getId() == null ? null : InventoryId.of(dataObject.getId()),
                dataObject.getSkuId() == null ? null : SkuId.of(dataObject.getSkuId()),
                dataObject.getWarehouseCode() == null ? null : WarehouseCode.of(dataObject.getWarehouseCode()),
                OnHandQuantity.of(dataObject.getOnHandQuantity()),
                ReservedQuantity.of(dataObject.getReservedQuantity()),
                InventoryStatus.from(dataObject.getStatus()),
                dataObject.getVersion() == null ? null : new Version(dataObject.getVersion()),
                dataObject.getUpdatedAt() == null ? dataObject.getCreatedAt() : dataObject.getUpdatedAt());
    }

    public static InventoryDO toDataObject(Inventory inventory) {
        return new InventoryDO(
                inventory.getId() == null ? null : inventory.getId().value(),
                BaconIdContextHelper.requireTenantId().value(),
                inventory.getSkuId() == null ? null : inventory.getSkuId().value(),
                inventory.getWarehouseCode() == null
                        ? null
                        : inventory.getWarehouseCode().value(),
                inventory.getOnHandQuantity() == null ? null : inventory.getOnHandQuantity().value(),
                inventory.getReservedQuantity() == null ? null : inventory.getReservedQuantity().value(),
                inventory.getStatus().value(),
                inventory.getVersion() == null ? null : inventory.getVersion().value(),
                null,
                inventory.getUpdatedAt(),
                null,
                inventory.getUpdatedAt());
    }
}
