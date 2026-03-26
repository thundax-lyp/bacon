package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryDeductionApplicationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryOperationLogService inventoryOperationLogService;
    private final InventoryTransactionExecutor inventoryTransactionExecutor;
    private final InventoryWriteRetrier inventoryWriteRetrier;

    @Autowired
    public InventoryDeductionApplicationService(InventoryStockRepository inventoryStockRepository,
                                                InventoryReservationRepository inventoryReservationRepository,
                                                InventoryOperationLogService inventoryOperationLogService,
                                                InventoryTransactionExecutor inventoryTransactionExecutor,
                                                InventoryWriteRetrier inventoryWriteRetrier) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
        this.inventoryTransactionExecutor = inventoryTransactionExecutor;
        this.inventoryWriteRetrier = inventoryWriteRetrier;
    }

    public InventoryDeductionApplicationService(InventoryStockRepository inventoryStockRepository,
                                                InventoryReservationRepository inventoryReservationRepository,
                                                InventoryOperationLogService inventoryOperationLogService) {
        this(inventoryStockRepository, inventoryReservationRepository, inventoryOperationLogService,
                new InventoryTransactionExecutor(), new InventoryWriteRetrier());
    }

    public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
        return inventoryWriteRetrier.execute("deduct", tenantId + ":" + orderNo, () ->
                inventoryTransactionExecutor.executeInNewTransaction(() ->
                        deductReservedStockOnce(tenantId, orderNo)));
    }

    private InventoryReservationResultDTO deductReservedStockOnce(Long tenantId, String orderNo) {
        InventoryReservation reservation = inventoryReservationRepository.findReservation(tenantId, orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultMapper.failed(tenantId, orderNo, InventoryErrorCode.RESERVATION_NOT_FOUND.code());
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultMapper.fromReservation(reservation);
        }

        Instant deductedAt = Instant.now();
        reservation.getItems().forEach(item -> {
            deductStockOnce(tenantId, item.getSkuId(), item.getQuantity(), deductedAt);
        });
        reservation.deduct(deductedAt);
        inventoryReservationRepository.saveReservation(reservation);
        inventoryOperationLogService.recordDeductSuccess(reservation, deductedAt);
        return InventoryReservationResultMapper.fromReservation(reservation);
    }

    private void deductStockOnce(Long tenantId, Long skuId, int quantity, Instant operatedAt) {
        Inventory inventory = inventoryStockRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId)));
        inventory.deduct(quantity, operatedAt);
        inventoryStockRepository.saveInventory(inventory);
    }
}
