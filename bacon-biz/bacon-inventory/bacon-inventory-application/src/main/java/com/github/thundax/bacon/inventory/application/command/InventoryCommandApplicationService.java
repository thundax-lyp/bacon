package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationResultAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryStockAssembler;
import com.github.thundax.bacon.inventory.application.audit.InventoryOperationLogSupport;
import com.github.thundax.bacon.inventory.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.inventory.application.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.result.InventoryReservationResult;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.application.support.InventoryWriteRetrier;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReservationStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryCommandApplicationService {

    private static final String INVENTORY_ID_BIZ_TAG = "inventory-id";
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
    public InventoryCommandApplicationService(
            InventoryStockRepository inventoryStockRepository,
            InventoryReservationRepository inventoryReservationRepository,
            InventoryAuditRecordRepository inventoryAuditRecordRepository,
            InventoryAuditOutboxRepository inventoryAuditOutboxRepository,
            InventoryReservationNoGenerator inventoryReservationNoGenerator,
            IdGenerator idGenerator) {
        this(
                inventoryStockRepository,
                inventoryReservationRepository,
                new InventoryOperationLogSupport(
                        inventoryAuditRecordRepository, inventoryAuditOutboxRepository, idGenerator),
                inventoryReservationNoGenerator,
                new InventoryTransactionExecutor(),
                new InventoryWriteRetrier(),
                idGenerator);
    }

    public InventoryCommandApplicationService(
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

    @Transactional
    public InventoryStockDTO create(InventoryCreateCommand command) {
        requireTenantContext();
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.skuId(), "skuId must not be null");
        Objects.requireNonNull(command.onHandQuantity(), "onHandQuantity must not be null");
        Objects.requireNonNull(command.status(), "status must not be null");
        inventoryStockRepository.findBySkuId(command.skuId()).ifPresent(inventory -> {
            throw new InventoryDomainException(
                    InventoryErrorCode.INVENTORY_ALREADY_EXISTS, String.valueOf(command.skuId()));
        });
        Inventory inventory = Inventory.create(
                InventoryId.of(idGenerator.nextId(INVENTORY_ID_BIZ_TAG)),
                command.skuId(),
                WarehouseCode.DEFAULT,
                OnHandQuantity.of(command.onHandQuantity()));
        if (!InventoryStatus.ENABLED.equals(command.status())) {
            inventory.updateStatus(command.status());
        }
        try {
            Inventory savedInventory = inventoryStockRepository.insert(inventory);
            return InventoryStockAssembler.fromInventory(savedInventory);
        } catch (DuplicateKeyException ex) {
            throw new InventoryDomainException(
                    InventoryErrorCode.INVENTORY_ALREADY_EXISTS, String.valueOf(command.skuId()), ex);
        }
    }

    @Transactional
    public InventoryStockDTO updateStatus(InventoryStatusUpdateCommand command) {
        requireTenantContext();
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.skuId(), "skuId must not be null");
        Objects.requireNonNull(command.status(), "status must not be null");
        Inventory inventory = inventoryStockRepository
                .findBySkuId(command.skuId())
                .orElseThrow(() -> new InventoryDomainException(
                        InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(command.skuId())));
        inventory.updateStatus(command.status());
        Inventory savedInventory = inventoryStockRepository.update(inventory);
        return InventoryStockAssembler.fromInventory(savedInventory);
    }

    public InventoryReservationResult reserveStock(InventoryReserveStockCommand command) {
        Long tenantId = BaconContextHolder.requireTenantId();
        Objects.requireNonNull(command, "command must not be null");
        return inventoryWriteRetrier.execute(
                "reserve",
                tenantId + ":" + command.orderNo(),
                () -> inventoryTransactionExecutor.executeInNewTransaction(() -> reserveStockOnce(command)));
    }

    public InventoryReservationResult releaseReservedStock(InventoryReleaseStockCommand command) {
        Long tenantId = BaconContextHolder.requireTenantId();
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.orderNo(), "orderNo must not be null");
        Objects.requireNonNull(command.reason(), "reason must not be null");
        return inventoryWriteRetrier.execute(
                "release",
                tenantId + ":" + command.orderNo(),
                () -> inventoryTransactionExecutor.executeInNewTransaction(
                        () -> releaseReservedStockOnce(command.orderNo(), command.reason())));
    }

    public InventoryReservationResult deductReservedStock(InventoryDeductStockCommand command) {
        Long tenantId = BaconContextHolder.requireTenantId();
        Objects.requireNonNull(command, "command must not be null");
        return inventoryWriteRetrier.execute(
                "deduct",
                tenantId + ":" + command.orderNo(),
                () -> inventoryTransactionExecutor.executeInNewTransaction(
                        () -> deductReservedStockOnce(command.orderNo())));
    }

    private InventoryReservationResult reserveStockOnce(InventoryReserveStockCommand command) {
        InventoryReservation existingReservation =
                inventoryReservationRepository.findByOrderNo(command.orderNo()).orElse(null);
        if (existingReservation != null) {
            if (InventoryReservationStatus.CREATED.equals(existingReservation.getReservationStatus())) {
                return completeCreatedReservation(existingReservation);
            }
            return InventoryReservationResultAssembler.fromReservation(existingReservation);
        }
        return createReservation(command.orderNo(), command.items());
    }

    private InventoryReservationResult createReservation(OrderNo orderNo, List<InventoryReservationItemCommand> items) {
        String reservationNo = inventoryReservationNoGenerator.nextReservationNo();
        ReservationNo reservationNoValue = ReservationNoCodec.toDomain(reservationNo);
        WarehouseCode warehouseCodeValue = WarehouseCode.DEFAULT;
        List<InventoryReservationItemCommand> normalizedItems = normalizeItems(items);
        List<InventoryReservationItem> reservationItems = normalizedItems.stream()
                .map(item -> InventoryReservationItem.create(
                        idGenerator.nextId(RESERVATION_ITEM_ID_BIZ_TAG),
                        reservationNoValue,
                        item.skuId(),
                        item.quantity()))
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
                inventoryReservationRepository.findByOrderNo(orderNo).orElse(null);
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
        reservation = inventoryReservationRepository.update(reservation);
        inventoryOperationLogService.recordReserveSuccess(reservation, operatedAt);
        return InventoryReservationResultAssembler.fromReservation(reservation);
    }

    private List<InventoryReservationItemCommand> normalizeItems(List<InventoryReservationItemCommand> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        Map<Long, Integer> quantityBySku = new LinkedHashMap<>();
        for (InventoryReservationItemCommand item : items) {
            quantityBySku.merge(
                        item.skuId() == null ? null : item.skuId().value(),
                    item.quantity(),
                    Integer::sum);
        }
        return quantityBySku.entrySet().stream()
                .map(entry -> new InventoryReservationItemCommand(
                        entry.getKey() == null ? null : SkuId.of(entry.getKey()), entry.getValue()))
                .toList();
    }

    private ReservationValidationResult validateReservation(List<InventoryReservationItemCommand> items) {
        if (items.isEmpty()) {
            return ReservationValidationResult.failed(InventoryErrorCode.INVALID_QUANTITY.code());
        }
        Set<Long> skuIds = items.stream()
                .map(InventoryReservationItemCommand::skuId)
                .filter(Objects::nonNull)
                .map(SkuId::value)
                .collect(Collectors.toSet());
        Map<Long, Inventory> inventoryBySku = inventoryStockRepository
                .listBySkuIds(skuIds.stream().map(SkuId::of).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(
                        inventory -> inventory.getSkuId() == null ? null : inventory.getSkuId().value(),
                        inventory -> inventory));
        for (InventoryReservationItemCommand item : items) {
            if (item.skuId() == null || item.quantity() == null || item.quantity() <= 0) {
                return ReservationValidationResult.failed(InventoryErrorCode.INVALID_QUANTITY.code());
            }
            try {
                Inventory inventory = inventoryBySku.get(item.skuId().value());
                if (inventory == null) {
                    throw new InventoryDomainException(
                            InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(item.skuId().value()));
                }
                if (InventoryStatus.DISABLED.equals(inventory.getStatus())) {
                    throw new InventoryDomainException(
                            InventoryErrorCode.INVENTORY_DISABLED, String.valueOf(item.skuId().value()));
                }
                if (!inventory.availableQuantity().isEnough(item.quantity())) {
                    throw new InventoryDomainException(
                            InventoryErrorCode.INSUFFICIENT_STOCK, String.valueOf(item.skuId().value()));
                }
            } catch (InventoryDomainException ex) {
                return ReservationValidationResult.failed(ex.getCode());
            }
        }
        return ReservationValidationResult.success(inventoryBySku);
    }

    private InventoryReservation saveReservationWithIdempotentFallback(InventoryReservation reservation) {
        try {
            return inventoryReservationRepository.insert(reservation);
        } catch (DuplicateKeyException ex) {
            return inventoryReservationRepository
                    .findByOrderNo(reservation.getOrderNo())
                    .orElseThrow(() -> ex);
        }
    }

    private void reserveStockOnce(InventoryReservationItem item, Instant operatedAt, Map<Long, Inventory> inventoryBySku) {
        Long skuId = SkuIdCodec.toValue(item.getSkuId());
        Inventory inventory = inventoryBySku.get(skuId);
        if (inventory == null) {
            inventory = inventoryStockRepository
                    .findBySkuId(item.getSkuId())
                    .orElseThrow(() -> new InventoryDomainException(
                            InventoryErrorCode.INVENTORY_NOT_FOUND,
                            String.valueOf(SkuIdCodec.toValue(item.getSkuId()))));
            inventoryBySku.put(skuId, inventory);
        }
        inventory.reserve(item.getQuantity());
        Inventory persistedInventory = inventoryStockRepository.update(inventory);
        inventoryBySku.put(skuId, persistedInventory);
    }

    private InventoryReservationResult completeCreatedReservation(InventoryReservation reservation) {
        List<InventoryReservationItemCommand> items = InventoryReservationAssembler.toItemDtos(reservation.getItems())
                .stream()
                .map(item -> new InventoryReservationItemCommand(
                        item.getSkuId() == null ? null : SkuId.of(item.getSkuId()),
                        item.getQuantity()))
                .toList();
        ReservationValidationResult validationResult = validateReservation(items);
        String failureReason = validationResult.failureReason();
        if (failureReason != null) {
            reservation.fail(failureReason);
            InventoryReservation persisted = inventoryReservationRepository.update(reservation);
            inventoryOperationLogService.recordReserveFailed(persisted, Instant.now());
            return InventoryReservationResultAssembler.fromReservation(persisted);
        }
        Instant operatedAt = Instant.now();
        Map<Long, Inventory> inventoryBySku = new HashMap<>(validationResult.inventoryBySku());
        for (InventoryReservationItem item : reservation.getItems()) {
            reserveStockOnce(item, operatedAt, inventoryBySku);
        }
        reservation.reserve();
        InventoryReservation persisted = inventoryReservationRepository.update(reservation);
        inventoryOperationLogService.recordReserveSuccess(persisted, operatedAt);
        return InventoryReservationResultAssembler.fromReservation(persisted);
    }

    private InventoryReservationResult releaseReservedStockOnce(OrderNo orderNo, InventoryReleaseReason reason) {
        InventoryReservation reservation = inventoryReservationRepository.findByOrderNo(orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultAssembler.failed(
                    orderNo == null ? null : orderNo.value(), InventoryErrorCode.RESERVATION_NOT_FOUND.code());
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultAssembler.fromReservation(reservation);
        }

        Instant releasedAt = Instant.now();
        reservation.getItems().forEach(item -> releaseStockOnce(item.getSkuId(), item.getQuantity(), releasedAt));
        reservation.release(reason, releasedAt);
        inventoryReservationRepository.update(reservation);
        inventoryOperationLogService.recordReleaseSuccess(reservation, releasedAt);
        return InventoryReservationResultAssembler.fromReservation(reservation);
    }

    private void releaseStockOnce(SkuId skuId, int quantity, Instant operatedAt) {
        Inventory inventory = inventoryStockRepository
                .findBySkuId(skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(skuId)));
        inventory.release(quantity);
        inventoryStockRepository.update(inventory);
    }

    private InventoryReservationResult deductReservedStockOnce(OrderNo orderNo) {
        InventoryReservation reservation = inventoryReservationRepository.findByOrderNo(orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultAssembler.failed(
                    orderNo == null ? null : orderNo.value(), InventoryErrorCode.RESERVATION_NOT_FOUND.code());
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultAssembler.fromReservation(reservation);
        }

        Instant deductedAt = Instant.now();
        reservation.getItems().forEach(item -> deductStockOnce(item.getSkuId(), item.getQuantity(), deductedAt));
        reservation.deduct(deductedAt);
        inventoryReservationRepository.update(reservation);
        inventoryOperationLogService.recordDeductSuccess(reservation, deductedAt);
        return InventoryReservationResultAssembler.fromReservation(reservation);
    }

    private void deductStockOnce(SkuId skuId, int quantity, Instant operatedAt) {
        Inventory inventory = inventoryStockRepository
                .findBySkuId(skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(skuId)));
        inventory.deduct(quantity);
        inventoryStockRepository.update(inventory);
    }

    private void requireTenantContext() {
        BaconContextHolder.requireTenantId();
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
