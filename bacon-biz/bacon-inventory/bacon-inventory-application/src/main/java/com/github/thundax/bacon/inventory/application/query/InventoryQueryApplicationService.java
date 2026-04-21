package com.github.thundax.bacon.inventory.application.query;

import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
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

    public InventoryStockDTO getAvailableStock(InventoryAvailableStockQuery query) {
        BaconContextHolder.requireTenantId();
        SkuId skuId = query == null ? null : query.skuId();
        return InventoryStockAssembler.fromInventory(inventoryStockRepository
                .findBySkuId(skuId)
                .orElseThrow(() -> new InventoryDomainException(
                        InventoryErrorCode.INVENTORY_NOT_FOUND, String.valueOf(SkuIdCodec.toValue(skuId)))));
    }

    public List<InventoryStockDTO> batchGetAvailableStock(InventoryBatchAvailableStockQuery query) {
        BaconContextHolder.requireTenantId();
        Set<SkuId> skuIds = query == null || query.skuIds() == null ? Set.of() : query.skuIds();
        return inventoryStockRepository.listBySkuIds(skuIds == null ? Set.of() : skuIds).stream()
                .map(InventoryStockAssembler::fromInventory)
                .toList();
    }

    public PageResult<InventoryStockDTO> page(InventoryPageQuery query) {
        BaconContextHolder.requireTenantId();
        SkuId skuId = query == null ? null : query.getSkuId();
        InventoryStatus status = query == null ? null : query.getStatus();
        int normalizedPageNo = query == null ? 1 : query.getPageNo();
        int normalizedPageSize = query == null ? 20 : query.getPageSize();
        List<InventoryStockDTO> records =
                inventoryStockRepository.page(skuId, status, normalizedPageNo, normalizedPageSize).stream()
                        .map(InventoryStockAssembler::fromInventory)
                        .toList();
        long total = inventoryStockRepository.count(skuId, status);
        return new PageResult<>(records, total, normalizedPageNo, normalizedPageSize);
    }

    public InventoryReservationDTO getReservationByOrderNo(InventoryReservationQuery query) {
        BaconContextHolder.requireTenantId();
        OrderNo orderNo = query == null ? null : query.orderNo();
        InventoryReservation reservation = inventoryReservationRepository
                .findByOrderNo(orderNo)
                .orElseThrow(() -> new InventoryDomainException(
                        InventoryErrorCode.RESERVATION_NOT_FOUND, OrderNoCodec.toValue(orderNo)));
        return InventoryReservationAssembler.toDto(reservation);
    }

    public List<InventoryLedgerDTO> listLedgersByOrderNo(InventoryLedgerQuery query) {
        BaconContextHolder.requireTenantId();
        OrderNo orderNo = query == null ? null : query.orderNo();
        return inventoryAuditRecordRepository.listLedgers(orderNo).stream()
                .map(InventoryLedgerAssembler::toDto)
                .toList();
    }

    public List<InventoryAuditLogDTO> listAuditLogsByOrderNo(InventoryAuditLogQuery query) {
        BaconContextHolder.requireTenantId();
        OrderNo orderNo = query == null ? null : query.orderNo();
        return inventoryAuditRecordRepository.listLogs(orderNo).stream()
                .map(InventoryAuditLogAssembler::toDto)
                .toList();
    }

    public PageResult<InventoryAuditDeadLetterDTO> page(InventoryAuditDeadLetterPageQuery query) {
        BaconContextHolder.requireTenantId();
        OrderNo orderNo = query == null ? null : query.getOrderNo();
        InventoryAuditReplayStatus replayStatus = query == null ? null : query.getReplayStatus();
        int normalizedPageNo = query == null ? 1 : query.getPageNo();
        int normalizedPageSize = query == null ? 20 : query.getPageSize();
        List<InventoryAuditDeadLetterDTO> records =
                inventoryAuditDeadLetterRepository
                        .page(orderNo, replayStatus, normalizedPageNo, normalizedPageSize)
                        .stream()
                        .map(InventoryAuditDeadLetterAssembler::toDto)
                        .toList();
        long total = inventoryAuditDeadLetterRepository.count(orderNo, replayStatus);
        return new PageResult<>(records, total, normalizedPageNo, normalizedPageSize);
    }

}
