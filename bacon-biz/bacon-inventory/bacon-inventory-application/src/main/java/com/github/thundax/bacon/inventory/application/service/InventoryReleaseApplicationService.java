package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReleaseApplicationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryOperationLogService inventoryOperationLogService;

    public InventoryReleaseApplicationService(InventoryRepository inventoryRepository,
                                              InventoryOperationLogService inventoryOperationLogService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
    }

    @Transactional
    public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
        InventoryReservation reservation = inventoryRepository.findReservation(tenantId, orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultMapper.failed(tenantId, orderNo, "RESERVATION_NOT_FOUND");
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultMapper.fromReservation(reservation);
        }

        Instant releasedAt = Instant.now();
        reservation.getItems().forEach(item -> {
            Inventory inventory = inventoryRepository.findInventory(tenantId, item.getSkuId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found: " + item.getSkuId()));
            inventory.release(item.getQuantity(), releasedAt);
            inventoryRepository.saveInventory(inventory);
        });
        reservation.release(reason, releasedAt);
        inventoryRepository.saveReservation(reservation);
        inventoryOperationLogService.recordReleaseSuccess(reservation, releasedAt);
        return InventoryReservationResultMapper.fromReservation(reservation);
    }
}
