package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class InventoryManagementApplicationService {

    private final AtomicLong idGenerator = new AtomicLong(1000L);
    private final InventoryRepository inventoryRepository;

    public InventoryManagementApplicationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryStockDTO createInventory(Long tenantId, Long skuId, Integer onHandQuantity, String status) {
        inventoryRepository.findInventory(tenantId, skuId).ifPresent(inventory -> {
            throw new IllegalArgumentException("INVENTORY_ALREADY_EXISTS:" + skuId);
        });
        Inventory inventory = Inventory.create(idGenerator.getAndIncrement(), tenantId, skuId, onHandQuantity, status,
                Instant.now());
        return toStockDto(inventoryRepository.saveInventory(inventory));
    }

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
