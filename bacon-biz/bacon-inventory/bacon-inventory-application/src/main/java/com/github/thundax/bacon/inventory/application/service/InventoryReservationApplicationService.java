package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReservationApplicationService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MILLIS = 20L;

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryOperationLogService inventoryOperationLogService;
    private final InventoryReservationNoGenerator inventoryReservationNoGenerator;

    public InventoryReservationApplicationService(InventoryStockRepository inventoryStockRepository,
                                                  InventoryReservationRepository inventoryReservationRepository,
                                                  InventoryOperationLogService inventoryOperationLogService,
                                                  InventoryReservationNoGenerator inventoryReservationNoGenerator) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
        this.inventoryReservationNoGenerator = inventoryReservationNoGenerator;
    }

    @Transactional
    public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        return inventoryReservationRepository.findReservation(tenantId, orderNo)
                .map(InventoryReservationResultMapper::fromReservation)
                .orElseGet(() -> createReservation(tenantId, orderNo, items));
    }

    private InventoryReservationResultDTO createReservation(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        String reservationNo = inventoryReservationNoGenerator.nextReservationNo();
        List<InventoryReservationItemDTO> normalizedItems = normalizeItems(items);
        List<InventoryReservationItem> reservationItems = normalizedItems.stream()
                .map(item -> new InventoryReservationItem(null, tenantId, reservationNo,
                        item.getSkuId(), item.getQuantity()))
                .toList();
        InventoryReservation reservation = new InventoryReservation(null, tenantId, reservationNo,
                orderNo, 1L, Instant.now(), reservationItems);

        ReservationValidationResult validationResult = validateReservation(tenantId, normalizedItems);
        String failureReason = validationResult.failureReason();
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
        Map<Long, Inventory> inventoryBySku = new HashMap<>(validationResult.inventoryBySku());
        for (InventoryReservationItem item : reservationItems) {
            reserveStockWithRetry(tenantId, item, operatedAt, inventoryBySku);
        }
        reservation.reserve();
        reservation = inventoryReservationRepository.saveReservation(reservation);
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

    private ReservationValidationResult validateReservation(Long tenantId, List<InventoryReservationItemDTO> items) {
        if (items.isEmpty()) {
            return ReservationValidationResult.failed(InventoryErrorCode.INVALID_QUANTITY.code());
        }
        Set<Long> skuIds = items.stream()
                .map(InventoryReservationItemDTO::getSkuId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Long, Inventory> inventoryBySku = inventoryStockRepository.findInventories(tenantId, skuIds).stream()
                .collect(java.util.stream.Collectors.toMap(Inventory::getSkuId, inventory -> inventory));
        for (InventoryReservationItemDTO item : items) {
            if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                return ReservationValidationResult.failed(InventoryErrorCode.INVALID_QUANTITY.code());
            }
            try {
                Inventory inventory = inventoryBySku.get(item.getSkuId());
                if (inventory == null) {
                    throw new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                            String.valueOf(item.getSkuId()));
                }
                inventory.ensureReservable(item.getQuantity());
            } catch (InventoryDomainException ex) {
                return ReservationValidationResult.failed(ex.getCode());
            }
        }
        return ReservationValidationResult.success(inventoryBySku);
    }

    private InventoryReservation saveReservationWithIdempotentFallback(InventoryReservation reservation) {
        try {
            return inventoryReservationRepository.saveReservation(reservation);
        } catch (DuplicateKeyException ex) {
            return inventoryReservationRepository.findReservation(reservation.getTenantId(), reservation.getOrderNo())
                    .orElseThrow(() -> ex);
        }
    }

    private InventoryReservation tryFindExistingReservation(Long tenantId, String orderNo) {
        return inventoryReservationRepository.findReservation(tenantId, orderNo).orElse(null);
    }

    private void reserveStockWithRetry(Long tenantId,
                                       InventoryReservationItem item,
                                       Instant operatedAt,
                                       Map<Long, Inventory> inventoryBySku) {
        int attempt = 0;
        long backoffMillis = INITIAL_BACKOFF_MILLIS;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;
            Inventory inventory = inventoryBySku.get(item.getSkuId());
            if (inventory == null) {
                inventory = loadInventory(tenantId, item.getSkuId());
                inventoryBySku.put(item.getSkuId(), inventory);
            }
            inventory.reserve(item.getQuantity(), operatedAt);
            try {
                Inventory persistedInventory = inventoryStockRepository.saveInventory(inventory);
                inventoryBySku.put(item.getSkuId(), persistedInventory);
                return;
            } catch (InventoryDomainException ex) {
                if (!isConcurrentModified(ex) || attempt >= MAX_RETRY_ATTEMPTS) {
                    throw ex;
                }
                sleepBackoff(backoffMillis);
                backoffMillis = backoffMillis * 2;
                inventoryBySku.put(item.getSkuId(), loadInventory(tenantId, item.getSkuId()));
            }
        }
    }

    private Inventory loadInventory(Long tenantId, Long skuId) {
        return inventoryStockRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId)));
    }

    private boolean isConcurrentModified(InventoryDomainException exception) {
        return InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED.code().equals(exception.getCode());
    }

    private void sleepBackoff(long backoffMillis) {
        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED, "retry-interrupted");
        }
    }

    private record ReservationValidationResult(String failureReason, Map<Long, Inventory> inventoryBySku) {

        private static ReservationValidationResult failed(String failureReason) {
            return new ReservationValidationResult(failureReason, Map.of());
        }

        private static ReservationValidationResult success(Map<Long, Inventory> inventoryBySku) {
            return new ReservationValidationResult(null, Map.copyOf(inventoryBySku));
        }
    }
}
