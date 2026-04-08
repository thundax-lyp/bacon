package com.github.thundax.bacon.inventory.application.query;

import com.github.thundax.bacon.inventory.application.assembler.InventoryStockAssembler;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.mapper.TenantIdMapper;
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
import com.github.thundax.bacon.inventory.application.mapper.OrderNoMapper;
import com.github.thundax.bacon.common.id.mapper.SkuIdMapper;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
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
        return InventoryStockAssembler.fromInventory(inventoryStockRepository.findInventory(TenantIdMapper.toDomain(tenantId),
                        SkuIdMapper.toDomain(skuId))
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_NOT_FOUND,
                        String.valueOf(skuId))));
    }

    public List<InventoryStockDTO> batchGetAvailableStock(Long tenantId, Set<Long> skuIds) {
        return inventoryStockRepository.findInventories(TenantIdMapper.toDomain(tenantId),
                        skuIds == null ? Set.of() : skuIds.stream().map(SkuIdMapper::toDomain).collect(java.util.stream.Collectors.toSet()))
                .stream()
                .map(InventoryStockAssembler::fromInventory)
                .toList();
    }

    public InventoryPageResultDTO pageInventories(InventoryPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        InventoryStatus normalizedStatus = normalizeStatus(query.getStatus());
        List<InventoryStockDTO> records = inventoryStockRepository
                .pageInventories(TenantIdMapper.toDomain(query.getTenantId()), SkuIdMapper.toDomain(query.getSkuId()),
                        normalizedStatus, pageNo, pageSize).stream()
                .map(InventoryStockAssembler::fromInventory)
                .toList();
        long total = inventoryStockRepository.countInventories(TenantIdMapper.toDomain(query.getTenantId()),
                SkuIdMapper.toDomain(query.getSkuId()), normalizedStatus);
        return new InventoryPageResultDTO(records, total, pageNo, pageSize);
    }

    public InventoryReservationDTO getReservationByOrderNo(TenantId tenantId, OrderNo orderNo) {
        InventoryReservation reservation = inventoryReservationRepository.findReservation(tenantId, orderNo)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.RESERVATION_NOT_FOUND,
                        orderNo == null ? null : orderNo.value()));
        return toReservationDto(reservation);
    }

    public List<InventoryLedgerDTO> listLedgersByOrderNo(TenantId tenantId, OrderNo orderNo) {
        return inventoryAuditRecordRepository.findLedgers(tenantId, orderNo).stream()
                .map(this::toLedgerDto)
                .toList();
    }

    public List<InventoryAuditLogDTO> listAuditLogsByOrderNo(TenantId tenantId, OrderNo orderNo) {
        return inventoryAuditRecordRepository.findAuditLogs(tenantId, orderNo).stream()
                .map(this::toAuditLogDto)
                .toList();
    }

    public InventoryAuditDeadLetterPageResultDTO pageAuditDeadLetters(InventoryAuditDeadLetterPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        InventoryAuditReplayStatus replayStatus = normalizeReplayStatus(query.getReplayStatus());
        List<InventoryAuditDeadLetterDTO> records = inventoryAuditDeadLetterRepository
                .pageAuditDeadLetters(TenantIdMapper.toDomain(query.getTenantId()), OrderNoMapper.toDomain(query.getOrderNo()),
                        replayStatus, pageNo, pageSize)
                .stream()
                .map(this::toAuditDeadLetterDto)
                .toList();
        long total = inventoryAuditDeadLetterRepository.countAuditDeadLetters(TenantIdMapper.toDomain(query.getTenantId()),
                OrderNoMapper.toDomain(query.getOrderNo()), replayStatus);
        return new InventoryAuditDeadLetterPageResultDTO(records, total, pageNo, pageSize);
    }

    InventoryReservationDTO toReservationDto(InventoryReservation reservation) {
        return new InventoryReservationDTO(reservation.getTenantId() == null ? null : reservation.getTenantId().value(), reservation.getOrderNoValue(),
                reservation.getReservationNoValue(), reservation.getReservationStatusValue(), reservation.getWarehouseNoValue(),
                reservation.getItems().stream()
                        .map(item -> new InventoryReservationItemDTO(item.getSkuId() == null ? null : item.getSkuId().value(),
                                item.getQuantity()))
                        .toList(),
                reservation.getFailureReason(), reservation.getReleaseReasonValue(), reservation.getCreatedAt(),
                reservation.getReleasedAt(), reservation.getDeductedAt());
    }

    private InventoryLedgerDTO toLedgerDto(InventoryLedger ledger) {
        return new InventoryLedgerDTO(ledger.getId(), ledger.getTenantId() == null ? null : ledger.getTenantId().value(), ledger.getOrderNoValue(),
                ledger.getReservationNoValue(), ledger.getSkuId() == null ? null : ledger.getSkuId().value(),
                ledger.getWarehouseNoValue(),
                ledger.getLedgerTypeValue(),
                ledger.getQuantity(), ledger.getOccurredAt());
    }

    private InventoryAuditLogDTO toAuditLogDto(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDTO(auditLog.getId(), auditLog.getTenantId() == null ? null : auditLog.getTenantId().value(), auditLog.getOrderNoValue(),
                auditLog.getReservationNoValue(), auditLog.getActionTypeValue(), auditLog.getOperatorTypeValue(),
                auditLog.getOperatorId(), auditLog.getOccurredAt());
    }

    private InventoryAuditDeadLetterDTO toAuditDeadLetterDto(InventoryAuditDeadLetter deadLetter) {
        return new InventoryAuditDeadLetterDTO(deadLetter.getIdValue(), deadLetter.getOutboxIdValue(),
                deadLetter.getEventCodeValue(), deadLetter.getTenantId() == null ? null : deadLetter.getTenantId().value(),
                deadLetter.getOrderNoValue(), deadLetter.getReservationNoValue(), deadLetter.getActionTypeValue(),
                deadLetter.getOperatorTypeValue(), deadLetter.getOperatorId(), deadLetter.getOccurredAt(),
                deadLetter.getRetryCount(), deadLetter.getErrorMessage(), deadLetter.getDeadReason(),
                deadLetter.getDeadAt(), deadLetter.getReplayStatusValue(), deadLetter.getReplayCount(),
                deadLetter.getLastReplayAt(), deadLetter.getLastReplayResult(), deadLetter.getLastReplayError(),
                deadLetter.getReplayKey(), deadLetter.getReplayOperatorType(), deadLetter.getReplayOperatorId());
    }

    private InventoryAuditReplayStatus normalizeReplayStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return InventoryAuditReplayStatus.from(status.trim().toUpperCase(Locale.ROOT));
    }

    private InventoryStatus normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return InventoryStatus.from(status.trim().toUpperCase(Locale.ROOT));
    }

}
