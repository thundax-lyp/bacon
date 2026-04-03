package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationResultAssembler;
import com.github.thundax.bacon.inventory.application.audit.InventoryOperationLogSupport;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.application.support.InventoryWriteRetrier;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class InventoryReservationApplicationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryOperationLogSupport inventoryOperationLogService;
    private final InventoryReservationNoGenerator inventoryReservationNoGenerator;
    private final InventoryTransactionExecutor inventoryTransactionExecutor;
    private final InventoryWriteRetrier inventoryWriteRetrier;

    @Autowired
    public InventoryReservationApplicationService(InventoryStockRepository inventoryStockRepository,
                                                  InventoryReservationRepository inventoryReservationRepository,
                                                  InventoryOperationLogSupport inventoryOperationLogService,
                                                  InventoryReservationNoGenerator inventoryReservationNoGenerator,
                                                  InventoryTransactionExecutor inventoryTransactionExecutor,
                                                  InventoryWriteRetrier inventoryWriteRetrier) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
        this.inventoryReservationNoGenerator = inventoryReservationNoGenerator;
        this.inventoryTransactionExecutor = inventoryTransactionExecutor;
        this.inventoryWriteRetrier = inventoryWriteRetrier;
    }

    public InventoryReservationApplicationService(InventoryStockRepository inventoryStockRepository,
                                                  InventoryReservationRepository inventoryReservationRepository,
                                                  InventoryOperationLogSupport inventoryOperationLogService,
                                                  InventoryReservationNoGenerator inventoryReservationNoGenerator) {
        this(inventoryStockRepository, inventoryReservationRepository, inventoryOperationLogService,
                inventoryReservationNoGenerator, new InventoryTransactionExecutor(), new InventoryWriteRetrier());
    }

    public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        return inventoryWriteRetrier.execute("reserve", tenantId + ":" + orderNo, () ->
                inventoryTransactionExecutor.executeInNewTransaction(() ->
                        reserveStockOnce(tenantId, orderNo, items)));
    }

    private InventoryReservationResultDTO reserveStockOnce(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        InventoryReservation existingReservation = inventoryReservationRepository.findReservation(tenantId, orderNo).orElse(null);
        if (existingReservation != null) {
            if (InventoryReservation.STATUS_CREATED.equals(existingReservation.getReservationStatus())) {
                return completeCreatedReservation(existingReservation);
            }
            return InventoryReservationResultAssembler.fromReservation(existingReservation);
        }
        return createReservation(tenantId, orderNo, items);
    }

    private InventoryReservationResultDTO createReservation(Long tenantId, String orderNo,
                                                            List<InventoryReservationItemDTO> items) {
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
            return InventoryReservationResultAssembler.fromReservation(reservation);
        }

        InventoryReservation existing = tryFindExistingReservation(tenantId, orderNo);
        if (existing != null) {
            return InventoryReservationResultAssembler.fromReservation(existing);
        }

        Instant operatedAt = Instant.now();
        reservation = saveReservationWithIdempotentFallback(reservation);
        if (!reservation.getReservationNo().equals(reservationNo)) {
            if (InventoryReservation.STATUS_CREATED.equals(reservation.getReservationStatus())) {
                return completeCreatedReservation(reservation);
            }
            return InventoryReservationResultAssembler.fromReservation(reservation);
        }
        Map<Long, Inventory> inventoryBySku = new HashMap<>(validationResult.inventoryBySku());
        for (InventoryReservationItem item : reservationItems) {
            reserveStockOnce(tenantId, item, operatedAt, inventoryBySku);
        }
        reservation.reserve();
        reservation = inventoryReservationRepository.saveReservation(reservation);
        inventoryOperationLogService.recordReserveSuccess(reservation, operatedAt);
        return InventoryReservationResultAssembler.fromReservation(reservation);
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
                .collect(java.util.stream.Collectors.toMap(inventory -> inventory.getSkuId().value(), inventory -> inventory));
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

    private void reserveStockOnce(Long tenantId,
                                  InventoryReservationItem item,
                                  Instant operatedAt,
                                  Map<Long, Inventory> inventoryBySku) {
        Inventory inventory = inventoryBySku.get(item.getSkuId());
        if (inventory == null) {
            inventory = loadInventory(tenantId, item.getSkuId());
            inventoryBySku.put(item.getSkuId(), inventory);
        }
        inventory.reserve(item.getQuantity(), operatedAt);
        Inventory persistedInventory = inventoryStockRepository.saveInventory(inventory);
        inventoryBySku.put(item.getSkuId(), persistedInventory);
    }

    private InventoryReservationResultDTO completeCreatedReservation(InventoryReservation reservation) {
        List<InventoryReservationItemDTO> items = reservation.getItems().stream()
                .map(item -> new InventoryReservationItemDTO(item.getSkuId(), item.getQuantity()))
                .toList();
        ReservationValidationResult validationResult = validateReservation(reservation.getTenantId(), items);
        String failureReason = validationResult.failureReason();
        if (failureReason != null) {
            reservation.fail(failureReason);
            InventoryReservation persisted = inventoryReservationRepository.saveReservation(reservation);
            inventoryOperationLogService.recordReserveFailed(persisted, Instant.now());
            return InventoryReservationResultAssembler.fromReservation(persisted);
        }
        Instant operatedAt = Instant.now();
        Map<Long, Inventory> inventoryBySku = new HashMap<>(validationResult.inventoryBySku());
        for (InventoryReservationItem item : reservation.getItems()) {
            reserveStockOnce(reservation.getTenantId(), item, operatedAt, inventoryBySku);
        }
        reservation.reserve();
        InventoryReservation persisted = inventoryReservationRepository.saveReservation(reservation);
        inventoryOperationLogService.recordReserveSuccess(persisted, operatedAt);
        return InventoryReservationResultAssembler.fromReservation(persisted);
    }

    private Inventory loadInventory(Long tenantId, Long skuId) {
        return inventoryStockRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId)));
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
