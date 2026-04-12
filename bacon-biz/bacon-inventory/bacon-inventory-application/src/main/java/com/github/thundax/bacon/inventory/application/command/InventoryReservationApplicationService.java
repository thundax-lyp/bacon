package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.mapper.SkuIdMapper;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationResultAssembler;
import com.github.thundax.bacon.inventory.application.audit.InventoryOperationLogSupport;
import com.github.thundax.bacon.inventory.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.application.support.InventoryWriteRetrier;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReservationStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    public InventoryReservationApplicationService(
            InventoryStockRepository inventoryStockRepository,
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

    public InventoryReservationApplicationService(
            InventoryStockRepository inventoryStockRepository,
            InventoryReservationRepository inventoryReservationRepository,
            InventoryOperationLogSupport inventoryOperationLogService,
            InventoryReservationNoGenerator inventoryReservationNoGenerator) {
        this(
                inventoryStockRepository,
                inventoryReservationRepository,
                inventoryOperationLogService,
                inventoryReservationNoGenerator,
                new InventoryTransactionExecutor(),
                new InventoryWriteRetrier());
    }

    public InventoryReservationResultDTO reserveStock(OrderNo orderNo, List<InventoryReservationItemDTO> items) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return inventoryWriteRetrier.execute(
                "reserve",
                tenantId + ":" + orderNo,
                () -> inventoryTransactionExecutor.executeInNewTransaction(() -> reserveStockOnce(orderNo, items)));
    }

    private InventoryReservationResultDTO reserveStockOnce(OrderNo orderNo, List<InventoryReservationItemDTO> items) {
        InventoryReservation existingReservation =
                inventoryReservationRepository.findReservation(orderNo).orElse(null);
        if (existingReservation != null) {
            if (InventoryReservationStatus.CREATED.equals(existingReservation.getReservationStatus())) {
                return completeCreatedReservation(existingReservation);
            }
            return InventoryReservationResultAssembler.fromReservation(existingReservation);
        }
        return createReservation(orderNo, items);
    }

    private InventoryReservationResultDTO createReservation(OrderNo orderNo, List<InventoryReservationItemDTO> items) {
        String reservationNo = inventoryReservationNoGenerator.nextReservationNo();
        ReservationNo reservationNoValue = ReservationNoCodec.toDomain(reservationNo);
        WarehouseCode warehouseCodeValue = WarehouseCode.DEFAULT;
        List<InventoryReservationItemDTO> normalizedItems = normalizeItems(items);
        List<InventoryReservationItem> reservationItems = InventoryReservationAssembler.toDomainItems(
                reservationNoValue == null ? null : reservationNoValue.value(), normalizedItems);
        InventoryReservation reservation = new InventoryReservation(
                null,
                reservationNoValue,
                orderNo,
                warehouseCodeValue,
                Instant.now(),
                reservationItems,
                InventoryReservationStatus.CREATED,
                null,
                null,
                null,
                null);

        ReservationValidationResult validationResult = validateReservation(normalizedItems);
        String failureReason = validationResult.failureReason();
        if (failureReason != null) {
            reservation.fail(failureReason);
            reservation = saveReservationWithIdempotentFallback(reservation);
            inventoryOperationLogService.recordReserveFailed(reservation, Instant.now());
            return InventoryReservationResultAssembler.fromReservation(reservation);
        }

        InventoryReservation existing =
                inventoryReservationRepository.findReservation(orderNo).orElse(null);
        if (existing != null) {
            return InventoryReservationResultAssembler.fromReservation(existing);
        }

        Instant operatedAt = Instant.now();
        reservation = saveReservationWithIdempotentFallback(reservation);
        if (!reservationNo.equals(ReservationNoCodec.toValue(reservation.getReservationNo()))) {
            if (InventoryReservationStatus.CREATED.equals(reservation.getReservationStatus())) {
                return completeCreatedReservation(reservation);
            }
            return InventoryReservationResultAssembler.fromReservation(reservation);
        }
        Map<Long, Inventory> inventoryBySku = new HashMap<>(validationResult.inventoryBySku());
        for (InventoryReservationItem item : reservationItems) {
            reserveStockOnce(item, operatedAt, inventoryBySku);
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

    private ReservationValidationResult validateReservation(List<InventoryReservationItemDTO> items) {
        if (items.isEmpty()) {
            return ReservationValidationResult.failed(InventoryErrorCode.INVALID_QUANTITY.code());
        }
        Set<Long> skuIds = items.stream()
                .map(InventoryReservationItemDTO::getSkuId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Inventory> inventoryBySku =
                inventoryStockRepository
                        .findInventories(
                                skuIds.stream().map(SkuIdMapper::toDomain).collect(Collectors.toSet()))
                        .stream()
                        .collect(java.util.stream.Collectors.toMap(
                                inventory -> inventory.getSkuId() == null
                                        ? null
                                        : inventory.getSkuId().value(),
                                inventory -> inventory));
        for (InventoryReservationItemDTO item : items) {
            if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                return ReservationValidationResult.failed(InventoryErrorCode.INVALID_QUANTITY.code());
            }
            try {
                Inventory inventory = inventoryBySku.get(item.getSkuId());
                if (inventory == null) {
                    throw new InventoryDomainException(
                            InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(item.getSkuId()));
                }
                if (InventoryStatus.DISABLED.equals(inventory.getStatus())) {
                    throw new InventoryDomainException(
                            InventoryErrorCode.INVENTORY_DISABLED, String.valueOf(item.getSkuId()));
                }
                if (!inventory.availableQuantity().isEnough(item.getQuantity())) {
                    throw new InventoryDomainException(
                            InventoryErrorCode.INSUFFICIENT_STOCK, String.valueOf(item.getSkuId()));
                }
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
            return inventoryReservationRepository
                    .findReservation(reservation.getOrderNo())
                    .orElseThrow(() -> ex);
        }
    }

    private void reserveStockOnce(
            InventoryReservationItem item, Instant operatedAt, Map<Long, Inventory> inventoryBySku) {
        Long skuId = SkuIdMapper.toValue(item.getSkuId());
        Inventory inventory = inventoryBySku.get(skuId);
        if (inventory == null) {
            inventory = inventoryStockRepository
                    .findInventory(item.getSkuId())
                    .orElseThrow(() -> new InventoryDomainException(
                            InventoryErrorCode.INVENTORY_NOT_FOUND,
                            String.valueOf(SkuIdMapper.toValue(item.getSkuId()))));
            inventoryBySku.put(skuId, inventory);
        }
        inventory.reserve(item.getQuantity());
        Inventory persistedInventory = inventoryStockRepository.saveInventory(inventory);
        inventoryBySku.put(skuId, persistedInventory);
    }

    private InventoryReservationResultDTO completeCreatedReservation(InventoryReservation reservation) {
        List<InventoryReservationItemDTO> items = InventoryReservationAssembler.toItemDtos(reservation.getItems());
        ReservationValidationResult validationResult = validateReservation(items);
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
            reserveStockOnce(item, operatedAt, inventoryBySku);
        }
        reservation.reserve();
        InventoryReservation persisted = inventoryReservationRepository.saveReservation(reservation);
        inventoryOperationLogService.recordReserveSuccess(persisted, operatedAt);
        return InventoryReservationResultAssembler.fromReservation(persisted);
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
