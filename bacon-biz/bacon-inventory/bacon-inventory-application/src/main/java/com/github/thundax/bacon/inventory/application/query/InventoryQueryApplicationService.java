package com.github.thundax.bacon.inventory.application.query;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterPageResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditLogDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryLedgerDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryPageResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.assembler.InventoryAuditDeadLetterAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryAuditLogAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryLedgerAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryStockAssembler;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class InventoryQueryApplicationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryAuditRecordRepository inventoryAuditRecordRepository;
    private final InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository;

    public InventoryQueryApplicationService(
            InventoryStockRepository inventoryStockRepository,
            InventoryReservationRepository inventoryReservationRepository,
            InventoryAuditRecordRepository inventoryAuditRecordRepository,
            InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryAuditRecordRepository = inventoryAuditRecordRepository;
        this.inventoryAuditDeadLetterRepository = inventoryAuditDeadLetterRepository;
    }

    public InventoryStockDTO getAvailableStock(SkuId skuId) {
        requireTenantContext();
        return InventoryStockAssembler.fromInventory(inventoryStockRepository
                .findInventory(skuId)
                .orElseThrow(() -> new InventoryDomainException(
                        InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(SkuIdCodec.toValue(skuId)))));
    }

    public List<InventoryStockDTO> batchGetAvailableStock(Set<SkuId> skuIds) {
        requireTenantContext();
        return inventoryStockRepository.findInventories(skuIds == null ? Set.of() : skuIds).stream()
                .map(InventoryStockAssembler::fromInventory)
                .toList();
    }

    public InventoryPageResultDTO pageInventories(
            SkuId skuId, InventoryStatus status, Integer pageNo, Integer pageSize) {
        requireTenantContext();
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        List<InventoryStockDTO> records =
                inventoryStockRepository.pageInventories(skuId, status, normalizedPageNo, normalizedPageSize).stream()
                        .map(InventoryStockAssembler::fromInventory)
                        .toList();
        long total = inventoryStockRepository.countInventories(skuId, status);
        return new InventoryPageResultDTO(records, total, normalizedPageNo, normalizedPageSize);
    }

    public InventoryReservationDTO getReservationByOrderNo(OrderNo orderNo) {
        requireTenantContext();
        InventoryReservation reservation = inventoryReservationRepository
                .findReservation(orderNo)
                .orElseThrow(() -> new InventoryDomainException(
                        InventoryErrorCode.RESERVATION_NOT_FOUND, OrderNoCodec.toValue(orderNo)));
        return InventoryReservationAssembler.toDto(reservation);
    }

    public List<InventoryLedgerDTO> listLedgersByOrderNo(OrderNo orderNo) {
        requireTenantContext();
        return inventoryAuditRecordRepository.findLedgers(orderNo).stream()
                .map(InventoryLedgerAssembler::toDto)
                .toList();
    }

    public List<InventoryAuditLogDTO> listAuditLogsByOrderNo(OrderNo orderNo) {
        requireTenantContext();
        return inventoryAuditRecordRepository.findAuditLogs(orderNo).stream()
                .map(InventoryAuditLogAssembler::toDto)
                .toList();
    }

    public InventoryAuditDeadLetterPageResultDTO pageAuditDeadLetters(
            OrderNo orderNo, InventoryAuditReplayStatus replayStatus, Integer pageNo, Integer pageSize) {
        requireTenantContext();
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        List<InventoryAuditDeadLetterDTO> records =
                inventoryAuditDeadLetterRepository
                        .pageAuditDeadLetters(orderNo, replayStatus, normalizedPageNo, normalizedPageSize)
                        .stream()
                        .map(InventoryAuditDeadLetterAssembler::toDto)
                        .toList();
        long total = inventoryAuditDeadLetterRepository.countAuditDeadLetters(orderNo, replayStatus);
        return new InventoryAuditDeadLetterPageResultDTO(records, total, normalizedPageNo, normalizedPageSize);
    }

    private void requireTenantContext() {
        BaconContextHolder.requireTenantId();
    }
}
