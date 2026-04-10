package com.github.thundax.bacon.inventory.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.valueobject.Version;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservedQuantity;
import org.junit.jupiter.api.Test;

class InventoryTest {

    @Test
    void createShouldInitializeDefaultState() {
        Inventory inventory =
                Inventory.create(
                        InventoryId.of(1L), SkuId.of(101L), WarehouseCode.DEFAULT, new OnHandQuantity(30));

        assertEquals(InventoryId.of(1L), inventory.getId());
        assertEquals(new OnHandQuantity(30), inventory.getOnHandQuantity());
        assertEquals(new ReservedQuantity(0), inventory.getReservedQuantity());
        assertEquals(30, inventory.availableQuantity().value());
        assertEquals(InventoryStatus.ENABLED, inventory.getStatus());
        assertEquals(new Version(0L), inventory.getVersion());
        assertNotNull(inventory.getUpdatedAt());
    }

    @Test
    void createShouldRejectNullInventoryKey() {
        InventoryDomainException exception = assertThrows(
                InventoryDomainException.class,
                () -> Inventory.create(
                        null, SkuId.of(101L), WarehouseCode.DEFAULT, new OnHandQuantity(30)));

        assertEquals(InventoryErrorCode.INVALID_INVENTORY_KEY.code(), exception.getCode());
    }

    @Test
    void createShouldRejectNullOnHandQuantity() {
        InventoryDomainException exception = assertThrows(
                InventoryDomainException.class,
                () -> Inventory.create(InventoryId.of(1L), SkuId.of(101L), WarehouseCode.DEFAULT, null));

        assertEquals(InventoryErrorCode.INVALID_ON_HAND_QUANTITY.code(), exception.getCode());
    }

    @Test
    void createShouldRejectNegativeOnHandQuantity() {
        InventoryDomainException exception = assertThrows(
                InventoryDomainException.class,
                () -> Inventory.create(
                        InventoryId.of(1L), SkuId.of(101L), WarehouseCode.DEFAULT, OnHandQuantity.of(-1)));

        assertEquals(InventoryErrorCode.INVALID_ON_HAND_QUANTITY.code(), exception.getCode());
    }

    @Test
    void onHandQuantityShouldIncreaseByDelta() {
        OnHandQuantity quantity = new OnHandQuantity(30);

        assertEquals(new OnHandQuantity(35), quantity.increase(5));
    }

    @Test
    void onHandQuantityShouldTellWhetherItIsZero() {
        assertEquals(true, new OnHandQuantity(0).isZero());
        assertEquals(false, new OnHandQuantity(1).isZero());
    }

    @Test
    void onHandQuantityShouldRejectNegativeIncreaseDelta() {
        InventoryDomainException exception =
                assertThrows(InventoryDomainException.class, () -> new OnHandQuantity(30).increase(-1));

        assertEquals(InventoryErrorCode.INVALID_DELTA_QUANTITY.code(), exception.getCode());
    }

    @Test
    void onHandQuantityShouldDecreaseByDelta() {
        OnHandQuantity quantity = new OnHandQuantity(30);

        assertEquals(new OnHandQuantity(25), quantity.decrease(5));
    }

    @Test
    void onHandQuantityShouldRejectNegativeDecreaseDelta() {
        InventoryDomainException exception =
                assertThrows(InventoryDomainException.class, () -> new OnHandQuantity(30).decrease(-1));

        assertEquals(InventoryErrorCode.INVALID_DELTA_QUANTITY.code(), exception.getCode());
    }

    @Test
    void onHandQuantityShouldRejectDecreaseWhenStockIsInsufficient() {
        InventoryDomainException exception =
                assertThrows(InventoryDomainException.class, () -> new OnHandQuantity(3).decrease(5));

        assertEquals(InventoryErrorCode.INSUFFICIENT_STOCK.code(), exception.getCode());
    }

    @Test
    void reservedQuantityShouldIncreaseByDelta() {
        ReservedQuantity quantity = new ReservedQuantity(3);

        assertEquals(new ReservedQuantity(5), quantity.increase(2));
    }

    @Test
    void reservedQuantityShouldTellWhetherItIsZero() {
        assertEquals(true, new ReservedQuantity(0).isZero());
        assertEquals(false, new ReservedQuantity(1).isZero());
    }

    @Test
    void reservedQuantityShouldRejectNegativeIncreaseDelta() {
        InventoryDomainException exception =
                assertThrows(InventoryDomainException.class, () -> new ReservedQuantity(3).increase(-1));

        assertEquals(InventoryErrorCode.INVALID_DELTA_QUANTITY.code(), exception.getCode());
    }

    @Test
    void reservedQuantityShouldDecreaseByDelta() {
        ReservedQuantity quantity = new ReservedQuantity(5);

        assertEquals(new ReservedQuantity(3), quantity.decrease(2));
    }

    @Test
    void reservedQuantityShouldRejectNegativeDecreaseDelta() {
        InventoryDomainException exception =
                assertThrows(InventoryDomainException.class, () -> new ReservedQuantity(3).decrease(-1));

        assertEquals(InventoryErrorCode.INVALID_DELTA_QUANTITY.code(), exception.getCode());
    }

    @Test
    void reservedQuantityShouldRejectDecreaseWhenQuantityIsNotEnough() {
        InventoryDomainException exception =
                assertThrows(InventoryDomainException.class, () -> new ReservedQuantity(3).decrease(5));

        assertEquals(InventoryErrorCode.RESERVED_QUANTITY_NOT_ENOUGH.code(), exception.getCode());
    }
}
