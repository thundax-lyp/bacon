package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
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
    public InventoryStockDTO createInventory(Long tenantId, Long skuId, Integer onHandQuantity, String status) {
        inventoryRepository.findInventory(tenantId, skuId).ifPresent(inventory -> {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS, String.valueOf(skuId));
        });
        Inventory inventory = Inventory.create(null, tenantId, skuId, onHandQuantity, status,
                Instant.now());
        try {
            return toStockDto(inventoryRepository.saveInventory(inventory));
        } catch (DuplicateKeyException ex) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS, String.valueOf(skuId), ex);
        }
    }

    @Transactional
    public InventoryStockDTO updateInventoryStatus(Long tenantId, Long skuId, String status) {
        Inventory inventory = inventoryRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId)));
        inventory.updateStatus(status, Instant.now());
        return toStockDto(inventoryRepository.saveInventory(inventory));
    }

    private InventoryStockDTO toStockDto(Inventory inventory) {
        return new InventoryStockDTO(Long.valueOf(inventory.getTenantId().value()), inventory.getSkuId().value(),
                inventory.getWarehouseIdValue(),
                inventory.getOnHandQuantity(), inventory.getReservedQuantity(), inventory.getAvailableQuantity(),
                inventory.getStatus().value(), inventory.getUpdatedAt());
    }
}
