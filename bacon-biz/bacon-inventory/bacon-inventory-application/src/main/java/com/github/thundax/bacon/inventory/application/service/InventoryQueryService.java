package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class InventoryQueryService {

    private final InventoryRepository inventoryRepository;

    public InventoryQueryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryStockDTO getAvailableStock(Long tenantId, Long skuId) {
        return toStockDto(inventoryRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + skuId)));
    }

    public List<InventoryStockDTO> batchGetAvailableStock(Long tenantId, Set<Long> skuIds) {
        return inventoryRepository.findInventories(tenantId, skuIds).stream().map(this::toStockDto).toList();
    }

    public InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo) {
        InventoryReservation reservation = inventoryRepository.findReservation(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + orderNo));
        return toReservationDto(reservation);
    }

    InventoryReservationDTO toReservationDto(InventoryReservation reservation) {
        return new InventoryReservationDTO(reservation.getTenantId(), reservation.getOrderNo(), reservation.getReservationNo(),
                reservation.getReservationStatus(), reservation.getWarehouseId(),
                reservation.getItems().stream()
                        .map(item -> new InventoryReservationItemDTO(item.getSkuId(), item.getQuantity()))
                        .toList(),
                reservation.getFailureReason(), reservation.getReleaseReason(), reservation.getCreatedAt(),
                reservation.getReleasedAt(), reservation.getDeductedAt());
    }

    private InventoryStockDTO toStockDto(Inventory inventory) {
        return new InventoryStockDTO(inventory.getTenantId(), inventory.getSkuId(), inventory.getWarehouseId(),
                inventory.getOnHandQuantity(), inventory.getReservedQuantity(), inventory.getAvailableQuantity(),
                inventory.getStatus(), inventory.getUpdatedAt());
    }
}
