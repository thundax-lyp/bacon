package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationAssembler;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationResultAssembler;
import com.github.thundax.bacon.inventory.application.audit.InventoryOperationLogSupport;
import com.github.thundax.bacon.inventory.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.inventory.application.result.InventoryReservationResult;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class InventoryReservationApplicationService {

    private static final String RESERVATION_ID_BIZ_TAG = "inventory-reservation-id";
    private static final String RESERVATION_ITEM_ID_BIZ_TAG = "inventory-reservation-item-id";

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryOperationLogSupport inventoryOperationLogService;
    private final InventoryReservationNoGenerator inventoryReservationNoGenerator;
    private final InventoryTransactionExecutor inventoryTransactionExecutor;
    private final InventoryWriteRetrier inventoryWriteRetrier;
    private final IdGenerator idGenerator;

    @Autowired
    public InventoryReservationApplicationService(
            InventoryStockRepository inventoryStockRepository,
            InventoryReservationRepository inventoryReservationRepository,
            InventoryOperationLogSupport inventoryOperationLogService,
            InventoryReservationNoGenerator inventoryReservationNoGenerator,
            InventoryTransactionExecutor inventoryTransactionExecutor,
            InventoryWriteRetrier inventoryWriteRetrier,
            IdGenerator idGenerator) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
        this.inventoryReservationNoGenerator = inventoryReservationNoGenerator;
        this.inventoryTransactionExecutor = inventoryTransactionExecutor;
        this.inventoryWriteRetrier = inventoryWriteRetrier;
        this.idGenerator = idGenerator;
    }

    public InventoryReservationApplicationService(
            InventoryStockRepository inventoryStockRepository,
            InventoryReservationRepository inventoryReservationRepository,
            InventoryOperationLogSupport inventoryOperationLogService,
            InventoryReservationNoGenerator inventoryReservationNoGenerator,
            IdGenerator idGenerator) {
        this(
                inventoryStockRepository,
                inventoryReservationRepository,
                inventoryOperationLogService,
                inventoryReservationNoGenerator,
                new InventoryTransactionExecutor(),
                new InventoryWriteRetrier(),
                idGenerator);
    }

    public InventoryReservationResult reserveStock(OrderNo orderNo, List<InventoryReservationItemDTO> items) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return inventoryWriteRetrier.execute(
                "reserve",
                tenantId + ":" + orderNo,
                () -> inventoryTransactionExecutor.executeInNewTransaction(() -> reserveStockOnce(orderNo, items)));
    }

    private InventoryReservationResult reserveStockOnce(OrderNo orderNo, List<InventoryReservationItemDTO> items) {
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

    private InventoryReservationResult createReservation(OrderNo orderNo, List<InventoryReservationItemDTO> items) {
        String reservationNo = inventoryReservationNoGenerator.nextReservationNo();
        ReservationNo reservationNoValue = ReservationNoCodec.toDomain(reservationNo);
        WarehouseCode warehouseCodeValue = WarehouseCode.DEFAULT;
        List<InventoryReservationItemDTO> normalizedItems = normalizeItems(items);
        List<InventoryReservationItem> reservationItems = normalizedItems.stream()
                .map(item -> InventoryReservationItem.create(
                        idGenerator.nextId(RESERVATION_ITEM_ID_BIZ_TAG),
                        reservationNoValue,
                        item.getSkuId() == null ? null : SkuId.of(item.getSkuId()),
                        item.getQuantity()))
                .toList();
        InventoryReservation reservation = InventoryReservation.create(
                idGenerator.nextId(RESERVATION_ID_BIZ_TAG),
                reservationNoValue,
                orderNo,
                warehouseCodeValue,
                Instant.now(),
                reservationItems);

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
        reservation = inventoryReservationRepository.upsertReservation(reservation);
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
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Inventory> inventoryBySku =
                inventoryStockRepository
                        .findInventories(
                                skuIds.stream().map(SkuIdCodec::toDomain).collect(Collectors.toSet()))
                        .stream()
                        .collect(Collectors.toMap(
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
            return inventoryReservationRepository.upsertReservation(reservation);
        } catch (DuplicateKeyException ex) {
            return inventoryReservationRepository
                    .findReservation(reservation.getOrderNo())
                    .orElseThrow(() -> ex);
        }
    }

    private void reserveStockOnce(
            InventoryReservationItem item, Instant operatedAt, Map<Long, Inventory> inventoryBySku) {
        Long skuId = SkuIdCodec.toValue(item.getSkuId());
        Inventory inventory = inventoryBySku.get(skuId);
        if (inventory == null) {
            inventory = inventoryStockRepository
                    .findInventory(item.getSkuId())
                    .orElseThrow(() -> new InventoryDomainException(
                            InventoryErrorCode.INVENTORY_NOT_FOUND,
                            String.valueOf(SkuIdCodec.toValue(item.getSkuId()))));
            inventoryBySku.put(skuId, inventory);
        }
        inventory.reserve(item.getQuantity());
        Inventory persistedInventory = inventoryStockRepository.upsertInventory(inventory);
        inventoryBySku.put(skuId, persistedInventory);
    }

    private InventoryReservationResult completeCreatedReservation(InventoryReservation reservation) {
        List<InventoryReservationItemDTO> items = InventoryReservationAssembler.toItemDtos(reservation.getItems());
        ReservationValidationResult validationResult = validateReservation(items);
        String failureReason = validationResult.failureReason();
        if (failureReason != null) {
            reservation.fail(failureReason);
            InventoryReservation persisted = inventoryReservationRepository.upsertReservation(reservation);
            inventoryOperationLogService.recordReserveFailed(persisted, Instant.now());
            return InventoryReservationResultAssembler.fromReservation(persisted);
        }
        Instant operatedAt = Instant.now();
        Map<Long, Inventory> inventoryBySku = new HashMap<>(validationResult.inventoryBySku());
        for (InventoryReservationItem item : reservation.getItems()) {
            reserveStockOnce(item, operatedAt, inventoryBySku);
        }
        reservation.reserve();
        InventoryReservation persisted = inventoryReservationRepository.upsertReservation(reservation);
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
