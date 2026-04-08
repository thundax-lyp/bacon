package com.github.thundax.bacon.inventory.application.query;

import com.github.thundax.bacon.inventory.application.assembler.InventoryStockAssembler;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditLogDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterPageQueryDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterPageResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryLedgerDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryPageQueryDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryPageResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class InventoryQueryApplicationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryAuditRecordRepository inventoryAuditRecordRepository;
    private final InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository;

    public InventoryQueryApplicationService(InventoryStockRepository inventoryStockRepository,
                                 InventoryReservationRepository inventoryReservationRepository,
                                 InventoryAuditRecordRepository inventoryAuditRecordRepository,
                                 InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryAuditRecordRepository = inventoryAuditRecordRepository;
        this.inventoryAuditDeadLetterRepository = inventoryAuditDeadLetterRepository;
    }

    public InventoryStockDTO getAvailableStock(Long tenantId, Long skuId) {
        return InventoryStockAssembler.fromInventory(inventoryStockRepository.findInventory(tenantId, skuId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId))));
    }

    public List<InventoryStockDTO> batchGetAvailableStock(Long tenantId, Set<Long> skuIds) {
        return inventoryStockRepository.findInventories(tenantId, skuIds).stream()
                .map(InventoryStockAssembler::fromInventory)
                .toList();
    }

    public InventoryPageResultDTO pageInventories(InventoryPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        String normalizedStatus = normalizeStatus(query.getStatus());
        List<InventoryStockDTO> records = inventoryStockRepository
                .pageInventories(query.getTenantId(), query.getSkuId(), normalizedStatus, pageNo, pageSize).stream()
                .map(InventoryStockAssembler::fromInventory)
                .toList();
        long total = inventoryStockRepository.countInventories(query.getTenantId(), query.getSkuId(), normalizedStatus);
        return new InventoryPageResultDTO(records, total, pageNo, pageSize);
    }

    public InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo) {
        InventoryReservation reservation = inventoryReservationRepository.findReservation(tenantId, orderNo)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.RESERVATION_NOT_FOUND, orderNo));
        return toReservationDto(reservation);
    }

    public List<InventoryLedgerDTO> listLedgersByOrderNo(Long tenantId, String orderNo) {
        return inventoryAuditRecordRepository.findLedgers(tenantId, orderNo).stream()
                .map(this::toLedgerDto)
                .toList();
    }

    public List<InventoryAuditLogDTO> listAuditLogsByOrderNo(Long tenantId, String orderNo) {
        return inventoryAuditRecordRepository.findAuditLogs(tenantId, orderNo).stream()
                .map(this::toAuditLogDto)
                .toList();
    }

    public InventoryAuditDeadLetterPageResultDTO pageAuditDeadLetters(InventoryAuditDeadLetterPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        String normalizedReplayStatus = normalizeStatus(query.getReplayStatus());
        List<InventoryAuditDeadLetterDTO> records = inventoryAuditDeadLetterRepository
                .pageAuditDeadLetters(query.getTenantId(), query.getOrderNo(), normalizedReplayStatus, pageNo, pageSize)
                .stream()
                .map(this::toAuditDeadLetterDto)
                .toList();
        long total = inventoryAuditDeadLetterRepository.countAuditDeadLetters(query.getTenantId(), query.getOrderNo(),
                normalizedReplayStatus);
        return new InventoryAuditDeadLetterPageResultDTO(records, total, pageNo, pageSize);
    }

    InventoryReservationDTO toReservationDto(InventoryReservation reservation) {
        return new InventoryReservationDTO(reservation.getTenantIdValue(), reservation.getOrderNoValue(),
                reservation.getReservationNoValue(), reservation.getReservationStatusValue(), reservation.getWarehouseNoValue(),
                reservation.getItems().stream()
                        .map(item -> new InventoryReservationItemDTO(item.getSkuIdValue(), item.getQuantity()))
                        .toList(),
                reservation.getFailureReason(), reservation.getReleaseReasonValue(), reservation.getCreatedAt(),
                reservation.getReleasedAt(), reservation.getDeductedAt());
    }

    private InventoryLedgerDTO toLedgerDto(InventoryLedger ledger) {
        return new InventoryLedgerDTO(ledger.getId(), ledger.getTenantIdValue(), ledger.getOrderNoValue(),
                ledger.getReservationNoValue(), ledger.getSkuIdValue(), ledger.getWarehouseNoValue(),
                ledger.getLedgerTypeValue(),
                ledger.getQuantity(), ledger.getOccurredAt());
    }

    private InventoryAuditLogDTO toAuditLogDto(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDTO(auditLog.getId(), auditLog.getTenantIdValue(), auditLog.getOrderNoValue(),
                auditLog.getReservationNoValue(), auditLog.getActionTypeValue(), auditLog.getOperatorTypeValue(),
                auditLog.getOperatorId(), auditLog.getOccurredAt());
    }

    private InventoryAuditDeadLetterDTO toAuditDeadLetterDto(InventoryAuditDeadLetter deadLetter) {
        return new InventoryAuditDeadLetterDTO(deadLetter.getIdValue(), deadLetter.getOutboxIdValue(),
                deadLetter.getEventCodeValue(), deadLetter.getTenantIdValue(),
                deadLetter.getOrderNoValue(), deadLetter.getReservationNoValue(), deadLetter.getActionTypeValue(),
                deadLetter.getOperatorTypeValue(), deadLetter.getOperatorId(), deadLetter.getOccurredAt(),
                deadLetter.getRetryCount(), deadLetter.getErrorMessage(), deadLetter.getDeadReason(),
                deadLetter.getDeadAt(), deadLetter.getReplayStatusValue(), deadLetter.getReplayCount(),
                deadLetter.getLastReplayAt(), deadLetter.getLastReplayResult(), deadLetter.getLastReplayError(),
                deadLetter.getReplayKey(), deadLetter.getReplayOperatorType(), deadLetter.getReplayOperatorId());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

}
