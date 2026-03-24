package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryDeductionApplicationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryOperationLogService inventoryOperationLogService;

    public InventoryDeductionApplicationService(InventoryRepository inventoryRepository,
                                                InventoryOperationLogService inventoryOperationLogService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
    }

    @Transactional
    public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
        InventoryReservation reservation = inventoryRepository.findReservation(tenantId, orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultMapper.failed(tenantId, orderNo, "RESERVATION_NOT_FOUND");
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultMapper.fromReservation(reservation);
        }

        Instant deductedAt = Instant.now();
        reservation.getItems().forEach(item -> {
            Inventory inventory = inventoryRepository.findInventory(tenantId, item.getSkuId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found: " + item.getSkuId()));
            inventory.deduct(item.getQuantity(), deductedAt);
            inventoryRepository.saveInventory(inventory);
        });
        reservation.deduct(deductedAt);
        inventoryRepository.saveReservation(reservation);
        inventoryOperationLogService.recordDeductSuccess(reservation, deductedAt);
        return InventoryReservationResultMapper.fromReservation(reservation);
    }
}
