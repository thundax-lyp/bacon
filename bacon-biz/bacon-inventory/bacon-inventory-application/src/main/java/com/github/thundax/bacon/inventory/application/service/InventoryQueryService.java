package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditLogDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryLedgerDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class InventoryQueryService {

    private final InventoryRepository inventoryRepository;

    public InventoryQueryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryStockDTO getAvailableStock(Long tenantId, Long skuId) {
        return toStockDto(inventoryRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + skuId)));
    }

    public List<InventoryStockDTO> batchGetAvailableStock(Long tenantId, Set<Long> skuIds) {
        return inventoryRepository.findInventories(tenantId, skuIds).stream().map(this::toStockDto).toList();
    }

    public InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo) {
        InventoryReservation reservation = inventoryRepository.findReservation(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + orderNo));
        return toReservationDto(reservation);
    }

    public List<InventoryLedgerDTO> listLedgersByOrderNo(Long tenantId, String orderNo) {
        return inventoryRepository.findLedgers(tenantId, orderNo).stream()
                .map(this::toLedgerDto)
                .toList();
    }

    public List<InventoryAuditLogDTO> listAuditLogsByOrderNo(Long tenantId, String orderNo) {
        return inventoryRepository.findAuditLogs(tenantId, orderNo).stream()
                .map(this::toAuditLogDto)
                .toList();
    }

    InventoryReservationDTO toReservationDto(InventoryReservation reservation) {
        return new InventoryReservationDTO(reservation.getTenantId(), reservation.getOrderNo(), reservation.getReservationNo(),
                reservation.getReservationStatus(), reservation.getWarehouseId(),
                reservation.getItems().stream()
                        .map(item -> new InventoryReservationItemDTO(item.getSkuId(), item.getQuantity()))
                        .toList(),
                reservation.getFailureReason(), reservation.getReleaseReason(), reservation.getCreatedAt(),
                reservation.getReleasedAt(), reservation.getDeductedAt());
    }

    private InventoryStockDTO toStockDto(Inventory inventory) {
        return new InventoryStockDTO(inventory.getTenantId(), inventory.getSkuId(), inventory.getWarehouseId(),
                inventory.getOnHandQuantity(), inventory.getReservedQuantity(), inventory.getAvailableQuantity(),
                inventory.getStatus(), inventory.getUpdatedAt());
    }

    private InventoryLedgerDTO toLedgerDto(InventoryLedger ledger) {
        return new InventoryLedgerDTO(ledger.getId(), ledger.getTenantId(), ledger.getOrderNo(),
                ledger.getReservationNo(), ledger.getSkuId(), ledger.getWarehouseId(), ledger.getLedgerType(),
                ledger.getQuantity(), ledger.getOccurredAt());
    }

    private InventoryAuditLogDTO toAuditLogDto(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDTO(auditLog.getId(), auditLog.getTenantId(), auditLog.getOrderNo(),
                auditLog.getReservationNo(), auditLog.getActionType(), auditLog.getOperatorType(),
                auditLog.getOperatorId(), auditLog.getOccurredAt());
    }
}
