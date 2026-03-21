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
        InventoryReservation reservation = inventoryRepository.findReservation(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + orderNo));
        reservation.release(reason, Instant.now());
        return new InventoryReservationResultDTO(tenantId, orderNo, reservation.getReservationNo(),
                reservation.getReservationStatus(), "RELEASED", reservation.getWarehouseId(),
                reservation.getFailureReason(), reservation.getReleaseReason(), reservation.getReleasedAt(), null);
    }
}
