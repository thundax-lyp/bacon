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
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryDeductionApplicationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryOperationLogSupport inventoryOperationLogService;
    private final InventoryTransactionExecutor inventoryTransactionExecutor;
    private final InventoryWriteRetrier inventoryWriteRetrier;

    @Autowired
    public InventoryDeductionApplicationService(
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

    public InventoryDeductionApplicationService(
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

    public InventoryReservationResult deductReservedStock(OrderNo orderNo) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return inventoryWriteRetrier.execute(
                "deduct",
                tenantId + ":" + orderNo,
                () -> inventoryTransactionExecutor.executeInNewTransaction(() -> deductReservedStockOnce(orderNo)));
    }

    private InventoryReservationResult deductReservedStockOnce(OrderNo orderNo) {
        InventoryReservation reservation =
                inventoryReservationRepository.findReservation(orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultAssembler.failed(
                    OrderNoCodec.toValue(orderNo), InventoryErrorCode.RESERVATION_NOT_FOUND.code());
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultAssembler.fromReservation(reservation);
        }

        Instant deductedAt = Instant.now();
        reservation.getItems().forEach(item -> {
            deductStockOnce(item.getSkuId(), item.getQuantity(), deductedAt);
        });
        reservation.deduct(deductedAt);
        inventoryReservationRepository.saveReservation(reservation);
        inventoryOperationLogService.recordDeductSuccess(reservation, deductedAt);
        return InventoryReservationResultAssembler.fromReservation(reservation);
    }

    private void deductStockOnce(SkuId skuId, int quantity, Instant operatedAt) {
        Inventory inventory = inventoryStockRepository
                .findInventory(skuId)
                .orElseThrow(() ->
                        new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(skuId)));
        inventory.deduct(quantity);
        inventoryStockRepository.saveInventory(inventory);
    }
}
