package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class InventoryReservationApplicationService {

    private final AtomicLong idGenerator = new AtomicLong(1L);
    private final InventoryRepository inventoryRepository;

    public InventoryReservationApplicationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        String reservationNo = "RSV-" + UUID.randomUUID().toString().substring(0, 8);
        List<InventoryReservationItem> reservationItems = items.stream()
                .map(item -> new InventoryReservationItem(idGenerator.getAndIncrement(), tenantId, reservationNo,
                        item.getSkuId(), item.getQuantity()))
                .toList();
        InventoryReservation reservation = new InventoryReservation(idGenerator.getAndIncrement(), tenantId, reservationNo,
                orderNo, 1L, Instant.now(), reservationItems);
        reservation.reserve();
        inventoryRepository.saveReservation(reservation);
        return new InventoryReservationResultDTO(tenantId, orderNo, reservationNo, reservation.getReservationStatus(),
                "RESERVED", 1L, null, null, null, null);
    }
}
