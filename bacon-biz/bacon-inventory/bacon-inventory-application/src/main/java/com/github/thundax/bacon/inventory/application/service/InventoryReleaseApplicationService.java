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
public class InventoryReleaseApplicationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryOperationLogService inventoryOperationLogService;
    private final InventoryTransactionExecutor inventoryTransactionExecutor;
    private final InventoryConcurrencyRetrier inventoryConcurrencyRetrier;

    @Autowired
    public InventoryReleaseApplicationService(InventoryStockRepository inventoryStockRepository,
                                              InventoryReservationRepository inventoryReservationRepository,
                                              InventoryOperationLogService inventoryOperationLogService,
                                              InventoryTransactionExecutor inventoryTransactionExecutor,
                                              InventoryConcurrencyRetrier inventoryConcurrencyRetrier) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
        this.inventoryTransactionExecutor = inventoryTransactionExecutor;
        this.inventoryConcurrencyRetrier = inventoryConcurrencyRetrier;
    }

    public InventoryReleaseApplicationService(InventoryStockRepository inventoryStockRepository,
                                              InventoryReservationRepository inventoryReservationRepository,
                                              InventoryOperationLogService inventoryOperationLogService) {
        this(inventoryStockRepository, inventoryReservationRepository, inventoryOperationLogService,
                new InventoryTransactionExecutor(), new InventoryConcurrencyRetrier());
    }

    public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
        return inventoryConcurrencyRetrier.execute(() ->
                inventoryTransactionExecutor.executeInNewTransaction(() ->
                        releaseReservedStockOnce(tenantId, orderNo, reason)));
    }

    private InventoryReservationResultDTO releaseReservedStockOnce(Long tenantId, String orderNo, String reason) {
        InventoryReservation reservation = inventoryReservationRepository.findReservation(tenantId, orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultMapper.failed(tenantId, orderNo, InventoryErrorCode.RESERVATION_NOT_FOUND.code());
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultMapper.fromReservation(reservation);
        }

        Instant releasedAt = Instant.now();
        reservation.getItems().forEach(item -> {
            releaseStockOnce(tenantId, item.getSkuId(), item.getQuantity(), releasedAt);
        });
        reservation.release(reason, releasedAt);
        inventoryReservationRepository.saveReservation(reservation);
        inventoryOperationLogService.recordReleaseSuccess(reservation, releasedAt);
        return InventoryReservationResultMapper.fromReservation(reservation);
    }

    private void releaseStockOnce(Long tenantId, Long skuId, int quantity, Instant operatedAt) {
        Inventory inventory = inventoryStockRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId)));
        inventory.release(quantity, operatedAt);
        inventoryStockRepository.saveInventory(inventory);
    }
}
