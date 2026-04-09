package com.github.thundax.bacon.inventory.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import org.junit.jupiter.api.Test;

class InventoryTest {

    @Test
    void createShouldInitializeDefaultState() {
        Inventory inventory =
                Inventory.create(InventoryId.of(1L), TenantId.of(1001L), SkuId.of(101L), WarehouseCode.DEFAULT, 30);

        assertEquals(InventoryId.of(1L), inventory.getId());
        assertEquals(30, inventory.getOnHandQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(30, inventory.getAvailableQuantity());
        assertEquals(InventoryStatus.ENABLED, inventory.getStatus());
        assertEquals(0L, inventory.getVersion());
        assertNotNull(inventory.getUpdatedAt());
    }

    @Test
    void createShouldRejectNullInventoryKey() {
        InventoryDomainException exception = assertThrows(
                InventoryDomainException.class,
                () -> Inventory.create(null, TenantId.of(1001L), SkuId.of(101L), WarehouseCode.DEFAULT, 30));

        assertEquals(InventoryErrorCode.INVALID_INVENTORY_KEY.code(), exception.getCode());
    }

    @Test
    void createShouldRejectNullOnHandQuantity() {
        InventoryDomainException exception = assertThrows(
                InventoryDomainException.class,
                () -> Inventory.create(
                        InventoryId.of(1L), TenantId.of(1001L), SkuId.of(101L), WarehouseCode.DEFAULT, null));

        assertEquals(InventoryErrorCode.INVALID_ON_HAND_QUANTITY.code(), exception.getCode());
    }

    @Test
    void createShouldRejectNegativeOnHandQuantity() {
        InventoryDomainException exception = assertThrows(
                InventoryDomainException.class,
                () -> Inventory.create(
                        InventoryId.of(1L), TenantId.of(1001L), SkuId.of(101L), WarehouseCode.DEFAULT, -1));

        assertEquals(InventoryErrorCode.INVALID_ON_HAND_QUANTITY.code(), exception.getCode());
    }
}
