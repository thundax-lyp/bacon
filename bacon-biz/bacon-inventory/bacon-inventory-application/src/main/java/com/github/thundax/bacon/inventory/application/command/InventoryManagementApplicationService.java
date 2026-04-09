package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.application.assembler.InventoryStockAssembler;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
import java.util.Objects;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryManagementApplicationService {

    private final InventoryStockRepository inventoryRepository;

    public InventoryManagementApplicationService(InventoryStockRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public InventoryStockDTO createInventory(TenantId tenantId, SkuId skuId, Integer onHandQuantity, InventoryStatus status) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(skuId, "skuId must not be null");
        Objects.requireNonNull(onHandQuantity, "onHandQuantity must not be null");
        Objects.requireNonNull(status, "status must not be null");
        inventoryRepository.findInventory(tenantId, skuId).ifPresent(inventory -> {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS, String.valueOf(skuId));
        });
        Instant now = Instant.now();
        Inventory inventory = new Inventory(null, tenantId, skuId, Inventory.DEFAULT_WAREHOUSE_CODE,
                onHandQuantity, 0, onHandQuantity, status, 0L, now);
        try {
            Inventory savedInventory = inventoryRepository.saveInventory(inventory);
            return InventoryStockAssembler.fromInventory(savedInventory);
        } catch (DuplicateKeyException ex) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS, String.valueOf(skuId), ex);
        }
    }

    @Transactional
    public InventoryStockDTO updateInventoryStatus(TenantId tenantId, SkuId skuId, InventoryStatus status) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(skuId, "skuId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Inventory inventory = inventoryRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId)));
        inventory.updateStatus(status, Instant.now());
        Inventory savedInventory = inventoryRepository.saveInventory(inventory);
        return InventoryStockAssembler.fromInventory(savedInventory);
    }

}
