package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryDeductionApplicationService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MILLIS = 20L;

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryOperationLogService inventoryOperationLogService;

    public InventoryDeductionApplicationService(InventoryStockRepository inventoryStockRepository,
                                                InventoryReservationRepository inventoryReservationRepository,
                                                InventoryOperationLogService inventoryOperationLogService) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryOperationLogService = inventoryOperationLogService;
    }

    @Transactional
    public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
        InventoryReservation reservation = inventoryReservationRepository.findReservation(tenantId, orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultMapper.failed(tenantId, orderNo, InventoryErrorCode.RESERVATION_NOT_FOUND.code());
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultMapper.fromReservation(reservation);
        }

        Instant deductedAt = Instant.now();
        reservation.getItems().forEach(item -> {
            deductStockWithRetry(tenantId, item.getSkuId(), item.getQuantity(), deductedAt);
        });
        reservation.deduct(deductedAt);
        inventoryReservationRepository.saveReservation(reservation);
        inventoryOperationLogService.recordDeductSuccess(reservation, deductedAt);
        return InventoryReservationResultMapper.fromReservation(reservation);
    }

    private void deductStockWithRetry(Long tenantId, Long skuId, int quantity, Instant operatedAt) {
        int attempt = 0;
        long backoffMillis = INITIAL_BACKOFF_MILLIS;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;
            Inventory inventory = inventoryStockRepository.findInventory(tenantId, skuId)
                    .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                            String.valueOf(skuId)));
            inventory.deduct(quantity, operatedAt);
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
