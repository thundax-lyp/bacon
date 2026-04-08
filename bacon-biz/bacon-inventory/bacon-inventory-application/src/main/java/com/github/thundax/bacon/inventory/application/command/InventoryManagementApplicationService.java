package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.inventory.application.assembler.InventoryStockAssembler;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
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
        validateInventoryKey(tenantId, skuId);
        validateOnHandQuantity(skuId, onHandQuantity);
        InventoryStatus normalizedStatus = normalizeStatus(status);
        Instant now = Instant.now();
        Inventory inventory = new Inventory(null, tenantId, skuId, Inventory.DEFAULT_WAREHOUSE_NO.value(),
                onHandQuantity, 0, onHandQuantity, normalizedStatus, 0L, now);
        try {
            return InventoryStockAssembler.fromInventory(inventoryRepository.saveInventory(inventory));
        } catch (DuplicateKeyException ex) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS, String.valueOf(skuId), ex);
        }
    }

    @Transactional
    public InventoryStockDTO updateInventoryStatus(Long tenantId, Long skuId, String status) {
        Inventory inventory = inventoryRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId)));
        inventory.updateStatus(normalizeStatus(status), Instant.now());
        return InventoryStockAssembler.fromInventory(inventoryRepository.saveInventory(inventory));
    }

    private void validateInventoryKey(Long tenantId, Long skuId) {
        if (tenantId == null || skuId == null) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_INVENTORY_KEY);
        }
    }

    private void validateOnHandQuantity(Long skuId, Integer onHandQuantity) {
        if (onHandQuantity == null || onHandQuantity < 0) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_ON_HAND_QUANTITY, String.valueOf(skuId));
        }
    }

    private InventoryStatus normalizeStatus(String status) {
        try {
            return InventoryStatus.from(status);
        } catch (IllegalArgumentException ex) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_INVENTORY_STATUS, String.valueOf(status));
        }
    }
}
