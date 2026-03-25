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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            reserveStockWithRetry(tenantId, item, operatedAt);
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

    private String validateReservation(Long tenantId, List<InventoryReservationItemDTO> items) {
        if (items.isEmpty()) {
            return InventoryErrorCode.INVALID_QUANTITY.code();
        }
        for (InventoryReservationItemDTO item : items) {
            if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                return InventoryErrorCode.INVALID_QUANTITY.code();
            }
            try {
                Inventory inventory = inventoryStockRepository.findInventory(tenantId, item.getSkuId())
                        .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                                String.valueOf(item.getSkuId())));
                inventory.ensureReservable(item.getQuantity());
            } catch (InventoryDomainException ex) {
                return ex.getCode();
            }
        }
        return null;
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

    private void reserveStockWithRetry(Long tenantId, InventoryReservationItem item, Instant operatedAt) {
        int attempt = 0;
        long backoffMillis = INITIAL_BACKOFF_MILLIS;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;
            Inventory inventory = inventoryStockRepository.findInventory(tenantId, item.getSkuId())
                    .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                            String.valueOf(item.getSkuId())));
            inventory.reserve(item.getQuantity(), operatedAt);
            try {
                inventoryStockRepository.saveInventory(inventory);
                return;
            } catch (InventoryDomainException ex) {
                if (!isConcurrentModified(ex) || attempt >= MAX_RETRY_ATTEMPTS) {
                    throw ex;
                }
                sleepBackoff(backoffMillis);
                backoffMillis = backoffMillis * 2;
            }
        }
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
}
