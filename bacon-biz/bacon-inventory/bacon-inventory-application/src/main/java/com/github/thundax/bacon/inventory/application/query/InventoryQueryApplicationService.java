package com.github.thundax.bacon.inventory.application.query;

import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.inventory.application.assembler.InventoryAuditDeadLetterAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryAuditLogAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryLedgerAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryReservationAssembler;
import com.github.thundax.bacon.inventory.application.assembler.InventoryStockAssembler;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.dto.InventoryAuditDeadLetterDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryAuditLogDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryLedgerDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.result.InventoryAuditDeadLetterPageResult;
import com.github.thundax.bacon.inventory.application.result.InventoryPageResult;
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
        BaconContextHolder.requireTenantId();
        return InventoryStockAssembler.fromInventory(inventoryStockRepository
                .findBySkuId(skuId)
                .orElseThrow(() -> new InventoryDomainException(
                        InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(SkuIdCodec.toValue(skuId)))));
    }

    public List<InventoryStockDTO> batchGetAvailableStock(Set<SkuId> skuIds) {
        BaconContextHolder.requireTenantId();
        return inventoryStockRepository.listBySkuIds(skuIds == null ? Set.of() : skuIds).stream()
                .map(InventoryStockAssembler::fromInventory)
                .toList();
    }

    public InventoryPageResult page(
            SkuId skuId, InventoryStatus status, Integer pageNo, Integer pageSize) {
        BaconContextHolder.requireTenantId();
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        List<InventoryStockDTO> records =
                inventoryStockRepository.page(skuId, status, normalizedPageNo, normalizedPageSize).stream()
                        .map(InventoryStockAssembler::fromInventory)
                        .toList();
        long total = inventoryStockRepository.count(skuId, status);
        return new InventoryPageResult(records, total, normalizedPageNo, normalizedPageSize);
    }

    public InventoryReservationDTO getReservationByOrderNo(OrderNo orderNo) {
        BaconContextHolder.requireTenantId();
        InventoryReservation reservation = inventoryReservationRepository
                .findByOrderNo(orderNo)
                .orElseThrow(() -> new InventoryDomainException(
                        InventoryErrorCode.RESERVATION_NOT_FOUND, OrderNoCodec.toValue(orderNo)));
        return InventoryReservationAssembler.toDto(reservation);
    }

    public List<InventoryLedgerDTO> listLedgersByOrderNo(OrderNo orderNo) {
        BaconContextHolder.requireTenantId();
        return inventoryAuditRecordRepository.listLedgers(orderNo).stream()
                .map(InventoryLedgerAssembler::toDto)
                .toList();
    }

    public List<InventoryAuditLogDTO> listAuditLogsByOrderNo(OrderNo orderNo) {
        BaconContextHolder.requireTenantId();
        return inventoryAuditRecordRepository.listLogs(orderNo).stream()
                .map(InventoryAuditLogAssembler::toDto)
                .toList();
    }

    public InventoryAuditDeadLetterPageResult page(
            OrderNo orderNo, InventoryAuditReplayStatus replayStatus, Integer pageNo, Integer pageSize) {
        BaconContextHolder.requireTenantId();
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        List<InventoryAuditDeadLetterDTO> records =
                inventoryAuditDeadLetterRepository
                        .page(orderNo, replayStatus, normalizedPageNo, normalizedPageSize)
                        .stream()
                        .map(InventoryAuditDeadLetterAssembler::toDto)
                        .toList();
        long total = inventoryAuditDeadLetterRepository.count(orderNo, replayStatus);
        return new InventoryAuditDeadLetterPageResult(records, total, normalizedPageNo, normalizedPageSize);
    }

}
