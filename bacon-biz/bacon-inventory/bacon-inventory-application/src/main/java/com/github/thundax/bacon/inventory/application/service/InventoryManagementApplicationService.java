package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryManagementApplicationService {

    private final InventoryRepository inventoryRepository;

    public InventoryManagementApplicationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public InventoryStockDTO createInventory(Long tenantId, Long skuId, Integer onHandQuantity, String status) {
        inventoryRepository.findInventory(tenantId, skuId).ifPresent(inventory -> {
            throw new IllegalArgumentException("INVENTORY_ALREADY_EXISTS:" + skuId);
        });
        Inventory inventory = Inventory.create(null, tenantId, skuId, onHandQuantity, status,
                Instant.now());
        try {
            return toStockDto(inventoryRepository.saveInventory(inventory));
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("INVENTORY_ALREADY_EXISTS:" + skuId, ex);
        }
    }

    @Transactional
    public InventoryStockDTO updateInventoryStatus(Long tenantId, Long skuId, String status) {
        Inventory inventory = inventoryRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + skuId));
        inventory.updateStatus(status, Instant.now());
        return toStockDto(inventoryRepository.saveInventory(inventory));
    }

    private InventoryStockDTO toStockDto(Inventory inventory) {
        return new InventoryStockDTO(inventory.getTenantId(), inventory.getSkuId(), inventory.getWarehouseId(),
                inventory.getOnHandQuantity(), inventory.getReservedQuantity(), inventory.getAvailableQuantity(),
                inventory.getStatus(), inventory.getUpdatedAt());
    }
}
