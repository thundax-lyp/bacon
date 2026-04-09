package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.mapper.TenantIdMapper;
import com.github.thundax.bacon.common.id.mapper.SkuIdMapper;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationResultAssembler;
import com.github.thundax.bacon.inventory.application.audit.InventoryOperationLogSupport;
import com.github.thundax.bacon.inventory.application.mapper.OrderNoMapper;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.application.support.InventoryWriteRetrier;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
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
    public InventoryReleaseApplicationService(InventoryStockRepository inventoryStockRepository,
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

    public InventoryReleaseApplicationService(InventoryStockRepository inventoryStockRepository,
                                              InventoryReservationRepository inventoryReservationRepository,
                                              InventoryOperationLogSupport inventoryOperationLogService) {
        this(inventoryStockRepository, inventoryReservationRepository, inventoryOperationLogService,
                new InventoryTransactionExecutor(), new InventoryWriteRetrier());
    }

    public InventoryReservationResultDTO releaseReservedStock(TenantId tenantId, OrderNo orderNo, String reason) {
        return inventoryWriteRetrier.execute("release", tenantId + ":" + orderNo, () ->
                inventoryTransactionExecutor.executeInNewTransaction(() ->
                        releaseReservedStockOnce(tenantId, orderNo, reason)));
    }

    private InventoryReservationResultDTO releaseReservedStockOnce(TenantId tenantId, OrderNo orderNo, String reason) {
        InventoryReservation reservation = inventoryReservationRepository.findReservation(tenantId, orderNo).orElse(null);
        if (reservation == null) {
            return InventoryReservationResultAssembler.failed(TenantIdMapper.toValue(tenantId),
                    OrderNoMapper.toValue(orderNo), InventoryErrorCode.RESERVATION_NOT_FOUND.code());
        }
        if (!reservation.isReserved()) {
            return InventoryReservationResultAssembler.fromReservation(reservation);
        }

        Instant releasedAt = Instant.now();
        InventoryReleaseReason releaseReason = toReleaseReason(reason);
        reservation.getItems().forEach(item -> {
            releaseStockOnce(tenantId, SkuIdMapper.toValue(item.getSkuId()), item.getQuantity(), releasedAt);
        });
        reservation.release(releaseReason, releasedAt);
        inventoryReservationRepository.saveReservation(reservation);
        inventoryOperationLogService.recordReleaseSuccess(reservation, releasedAt);
        return InventoryReservationResultAssembler.fromReservation(reservation);
    }

    private void releaseStockOnce(TenantId tenantId, Long skuId, int quantity, Instant operatedAt) {
        Inventory inventory = inventoryStockRepository.findInventory(tenantId, SkuIdMapper.toDomain(skuId))
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId)));
        inventory.release(quantity, operatedAt);
        inventoryStockRepository.saveInventory(inventory);
    }

    private InventoryReleaseReason toReleaseReason(String reason) {
        try {
            return InventoryReleaseReason.from(reason);
        } catch (IllegalArgumentException ex) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_RELEASE_REASON, reason);
        }
    }
}
