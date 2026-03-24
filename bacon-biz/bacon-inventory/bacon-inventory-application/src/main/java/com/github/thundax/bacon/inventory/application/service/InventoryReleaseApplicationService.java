package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class InventoryReleaseApplicationService {

    private final InventoryRepository inventoryRepository;

    public InventoryReleaseApplicationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
        InventoryReservation reservation = inventoryRepository.findReservation(tenantId, orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultMapper.failed(tenantId, orderNo, "RESERVATION_NOT_FOUND");
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultMapper.fromReservation(reservation);
        }

        Instant releasedAt = Instant.now();
        reservation.getItems().forEach(item -> inventoryRepository.findInventory(tenantId, item.getSkuId())
                .orElseThrow(() -> new IllegalStateException("Inventory not found: " + item.getSkuId()))
                .release(item.getQuantity(), releasedAt));
        reservation.release(reason, releasedAt);
        inventoryRepository.saveReservation(reservation);
        return InventoryReservationResultMapper.fromReservation(reservation);
    }
}
