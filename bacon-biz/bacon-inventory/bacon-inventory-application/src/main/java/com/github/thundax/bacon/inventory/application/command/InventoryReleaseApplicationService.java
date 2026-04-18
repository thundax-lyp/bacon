package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationResultAssembler;
import com.github.thundax.bacon.inventory.application.audit.InventoryOperationLogSupport;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.result.InventoryReservationResult;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.application.support.InventoryWriteRetrier;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryReleaseApplicationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryOperationLogSupport inventoryOperationLogService;
    private final InventoryTransactionExecutor inventoryTransactionExecutor;
    private final InventoryWriteRetrier inventoryWriteRetrier;

    @Autowired
    public InventoryReleaseApplicationService(
            InventoryStockRepository inventoryStockRepository,
            InventoryReservationRepository inventoryReservationRepository,
            InventoryOperationLogSupport inventoryOperationLogService,
            InventoryTransactionExecutor inventoryTransactionExecutor,
            InventoryWriteRetrier inventoryWriteRetrier) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
        this.inventoryTransactionExecutor = inventoryTransactionExecutor;
        this.inventoryWriteRetrier = inventoryWriteRetrier;
    }

    public InventoryReleaseApplicationService(
            InventoryStockRepository inventoryStockRepository,
            InventoryReservationRepository inventoryReservationRepository,
            InventoryOperationLogSupport inventoryOperationLogService) {
        this(
                inventoryStockRepository,
                inventoryReservationRepository,
                inventoryOperationLogService,
                new InventoryTransactionExecutor(),
                new InventoryWriteRetrier());
    }

    public InventoryReservationResult releaseReservedStock(OrderNo orderNo, InventoryReleaseReason reason) {
        Long tenantId = BaconContextHolder.requireTenantId();
        Objects.requireNonNull(orderNo, "orderNo must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        return inventoryWriteRetrier.execute(
                "release",
                tenantId + ":" + orderNo,
                () -> inventoryTransactionExecutor.executeInNewTransaction(
                        () -> releaseReservedStockOnce(orderNo, reason)));
    }

    private InventoryReservationResult releaseReservedStockOnce(OrderNo orderNo, InventoryReleaseReason reason) {
        InventoryReservation reservation =
                inventoryReservationRepository.findReservation(orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultAssembler.failed(
                    OrderNoCodec.toValue(orderNo), InventoryErrorCode.RESERVATION_NOT_FOUND.code());
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultAssembler.fromReservation(reservation);
        }

        Instant releasedAt = Instant.now();
        reservation.getItems().forEach(item -> {
            releaseStockOnce(item.getSkuId(), item.getQuantity(), releasedAt);
        });
        reservation.release(reason, releasedAt);
        inventoryReservationRepository.updateReservation(reservation);
        inventoryOperationLogService.recordReleaseSuccess(reservation, releasedAt);
        return InventoryReservationResultAssembler.fromReservation(reservation);
    }

    private void releaseStockOnce(SkuId skuId, int quantity, Instant operatedAt) {
        Inventory inventory = inventoryStockRepository
                .findInventory(skuId)
                .orElseThrow(() ->
                        new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(skuId)));
        inventory.release(quantity);
        inventoryStockRepository.updateInventory(inventory);
    }
}
