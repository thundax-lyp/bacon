package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class InventoryDeductionApplicationService {

    private final InventoryRepository inventoryRepository;

    public InventoryDeductionApplicationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
        InventoryReservation reservation = inventoryRepository.findReservation(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + orderNo));
        reservation.deduct(Instant.now());
        return new InventoryReservationResultDTO(tenantId, orderNo, reservation.getReservationNo(),
                reservation.getReservationStatus(), "DEDUCTED", reservation.getWarehouseId(),
                reservation.getFailureReason(), reservation.getReleaseReason(), reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }
}
