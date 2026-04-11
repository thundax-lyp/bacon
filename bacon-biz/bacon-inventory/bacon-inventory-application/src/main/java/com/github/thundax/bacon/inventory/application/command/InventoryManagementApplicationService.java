package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.assembler.InventoryStockAssembler;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
import java.util.Objects;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryManagementApplicationService {

    private static final String INVENTORY_ID_BIZ_TAG = "inventory-id";

    private final InventoryStockRepository inventoryRepository;
    private final IdGenerator idGenerator;

    public InventoryManagementApplicationService(
            InventoryStockRepository inventoryRepository, IdGenerator idGenerator) {
        this.inventoryRepository = inventoryRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public InventoryStockDTO createInventory(SkuId skuId, Integer onHandQuantity, InventoryStatus status) {
        requireTenantContext();
        Objects.requireNonNull(skuId, "skuId must not be null");
        Objects.requireNonNull(onHandQuantity, "onHandQuantity must not be null");
        Objects.requireNonNull(status, "status must not be null");
        inventoryRepository.findInventory(skuId).ifPresent(inventory -> {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS, String.valueOf(skuId));
        });
        Inventory inventory = Inventory.create(
                InventoryId.of(idGenerator.nextId(INVENTORY_ID_BIZ_TAG)),
                skuId,
                WarehouseCode.DEFAULT,
                OnHandQuantity.of(onHandQuantity));
        if (!InventoryStatus.ENABLED.equals(status)) {
            inventory.updateStatus(status);
        }
        try {
            Inventory savedInventory = inventoryRepository.saveInventory(inventory);
            return InventoryStockAssembler.fromInventory(savedInventory);
        } catch (DuplicateKeyException ex) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS, String.valueOf(skuId), ex);
        }
    }

    @Transactional
    public InventoryStockDTO updateInventoryStatus(SkuId skuId, InventoryStatus status) {
        requireTenantContext();
        Objects.requireNonNull(skuId, "skuId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Inventory inventory = inventoryRepository
                .findInventory(skuId)
                .orElseThrow(() ->
                        new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(skuId)));
        inventory.updateStatus(status);
        Inventory savedInventory = inventoryRepository.saveInventory(inventory);
        return InventoryStockAssembler.fromInventory(savedInventory);
    }

    private void requireTenantContext() {
        Long tenantId = BaconContextHolder.currentTenantId();
        Objects.requireNonNull(tenantId, "tenantId must not be null");
    }
}
