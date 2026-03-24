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
        InventoryReservation reservation = inventoryRepository.findReservation(tenantId, orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultMapper.failed(tenantId, orderNo, "RESERVATION_NOT_FOUND");
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultMapper.fromReservation(reservation);
        }

        Instant deductedAt = Instant.now();
        reservation.getItems().forEach(item -> inventoryRepository.findInventory(tenantId, item.getSkuId())
                .orElseThrow(() -> new IllegalStateException("Inventory not found: " + item.getSkuId()))
                .deduct(item.getQuantity(), deductedAt));
        reservation.deduct(deductedAt);
        inventoryRepository.saveReservation(reservation);
        return InventoryReservationResultMapper.fromReservation(reservation);
    }
}
