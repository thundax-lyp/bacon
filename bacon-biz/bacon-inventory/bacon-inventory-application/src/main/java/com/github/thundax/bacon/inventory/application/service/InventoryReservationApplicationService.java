package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReservationApplicationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryOperationLogService inventoryOperationLogService;

    public InventoryReservationApplicationService(InventoryRepository inventoryRepository,
                                                  InventoryOperationLogService inventoryOperationLogService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
    }

    @Transactional
    public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        return inventoryRepository.findReservation(tenantId, orderNo)
                .map(InventoryReservationResultMapper::fromReservation)
                .orElseGet(() -> createReservation(tenantId, orderNo, items));
    }

    private InventoryReservationResultDTO createReservation(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        String reservationNo = "RSV-" + UUID.randomUUID().toString().substring(0, 8);
        List<InventoryReservationItemDTO> normalizedItems = normalizeItems(items);
        List<InventoryReservationItem> reservationItems = normalizedItems.stream()
                .map(item -> new InventoryReservationItem(null, tenantId, reservationNo,
                        item.getSkuId(), item.getQuantity()))
                .toList();
        InventoryReservation reservation = new InventoryReservation(null, tenantId, reservationNo,
                orderNo, 1L, Instant.now(), reservationItems);

        String failureReason = validateReservation(tenantId, normalizedItems);
        if (failureReason != null) {
            reservation.fail(failureReason);
            reservation = saveReservationWithIdempotentFallback(reservation);
            inventoryOperationLogService.recordReserveFailed(reservation, Instant.now());
            return InventoryReservationResultMapper.fromReservation(reservation);
        }

        InventoryReservation existing = tryFindExistingReservation(tenantId, orderNo);
        if (existing != null) {
            return InventoryReservationResultMapper.fromReservation(existing);
        }

        Instant operatedAt = Instant.now();
        reservation = saveReservationWithIdempotentFallback(reservation);
        if (!reservation.getReservationNo().equals(reservationNo)) {
            return InventoryReservationResultMapper.fromReservation(reservation);
        }
        for (InventoryReservationItem item : reservationItems) {
            Inventory inventory = inventoryRepository.findInventory(tenantId, item.getSkuId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found: " + item.getSkuId()));
            inventory.reserve(item.getQuantity(), operatedAt);
            inventoryRepository.saveInventory(inventory);
        }
        reservation.reserve();
        reservation = inventoryRepository.saveReservation(reservation);
        inventoryOperationLogService.recordReserveSuccess(reservation, operatedAt);
        return InventoryReservationResultMapper.fromReservation(reservation);
    }

    private List<InventoryReservationItemDTO> normalizeItems(List<InventoryReservationItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        Map<Long, Integer> quantityBySku = new LinkedHashMap<>();
        for (InventoryReservationItemDTO item : items) {
            quantityBySku.merge(item.getSkuId(), item.getQuantity(), Integer::sum);
        }
        return quantityBySku.entrySet().stream()
                .map(entry -> new InventoryReservationItemDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    private String validateReservation(Long tenantId, List<InventoryReservationItemDTO> items) {
        if (items.isEmpty()) {
            return "EMPTY_ITEMS";
        }
        for (InventoryReservationItemDTO item : items) {
            if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                return "INVALID_ITEM";
            }
            try {
                Inventory inventory = inventoryRepository.findInventory(tenantId, item.getSkuId())
                        .orElseThrow(() -> new IllegalArgumentException("INVENTORY_NOT_FOUND:" + item.getSkuId()));
                inventory.ensureReservable(item.getQuantity());
            } catch (RuntimeException ex) {
                return ex.getMessage();
            }
        }
        return null;
    }

    private InventoryReservation saveReservationWithIdempotentFallback(InventoryReservation reservation) {
        try {
            return inventoryRepository.saveReservation(reservation);
        } catch (DuplicateKeyException ex) {
            return inventoryRepository.findReservation(reservation.getTenantId(), reservation.getOrderNo())
                    .orElseThrow(() -> ex);
        }
    }

    private InventoryReservation tryFindExistingReservation(Long tenantId, String orderNo) {
        return inventoryRepository.findReservation(tenantId, orderNo).orElse(null);
    }
}
