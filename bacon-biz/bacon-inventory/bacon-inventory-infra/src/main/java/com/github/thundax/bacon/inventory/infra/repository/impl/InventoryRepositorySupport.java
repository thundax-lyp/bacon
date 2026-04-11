package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import com.github.thundax.bacon.inventory.infra.persistence.assembler.InventoryAuditDeadLetterPersistenceAssembler;
import com.github.thundax.bacon.inventory.infra.persistence.assembler.InventoryAuditLogPersistenceAssembler;
import com.github.thundax.bacon.inventory.infra.persistence.assembler.InventoryAuditOutboxPersistenceAssembler;
import com.github.thundax.bacon.inventory.infra.persistence.assembler.InventoryAuditReplayTaskItemPersistenceAssembler;
import com.github.thundax.bacon.inventory.infra.persistence.assembler.InventoryAuditReplayTaskPersistenceAssembler;
import com.github.thundax.bacon.inventory.infra.persistence.assembler.InventoryLedgerPersistenceAssembler;
import com.github.thundax.bacon.inventory.infra.persistence.assembler.InventoryPersistenceAssembler;
import com.github.thundax.bacon.inventory.infra.persistence.assembler.InventoryReservationItemPersistenceAssembler;
import com.github.thundax.bacon.inventory.infra.persistence.assembler.InventoryReservationPersistenceAssembler;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditDeadLetterDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditLogDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditOutboxDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditReplayTaskDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditReplayTaskItemDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryLedgerDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationItemDO;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditDeadLetterMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditLogMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditOutboxMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditReplayTaskItemMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditReplayTaskMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryLedgerMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryReservationItemMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryReservationMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
public class InventoryRepositorySupport {

    private static final String AUDIT_OUTBOX_EVENT_CODE_BIZ_TAG = "inventory_audit_outbox_event_code";
    private static final DateTimeFormatter EVENT_CODE_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final InventoryMapper inventoryMapper;
    private final InventoryReservationMapper reservationMapper;
    private final InventoryReservationItemMapper reservationItemMapper;
    private final InventoryLedgerMapper ledgerMapper;
    private final InventoryAuditLogMapper auditLogMapper;
    private final InventoryAuditOutboxMapper auditOutboxMapper;
    private final InventoryAuditDeadLetterMapper auditDeadLetterMapper;
    private final InventoryAuditReplayTaskMapper auditReplayTaskMapper;
    private final InventoryAuditReplayTaskItemMapper auditReplayTaskItemMapper;
    private final IdGenerator idGenerator;

    public InventoryRepositorySupport(
            InventoryMapper inventoryMapper,
            InventoryReservationMapper reservationMapper,
            InventoryReservationItemMapper reservationItemMapper,
            InventoryLedgerMapper ledgerMapper,
            InventoryAuditLogMapper auditLogMapper,
            InventoryAuditOutboxMapper auditOutboxMapper,
            InventoryAuditDeadLetterMapper auditDeadLetterMapper,
            InventoryAuditReplayTaskMapper auditReplayTaskMapper,
            InventoryAuditReplayTaskItemMapper auditReplayTaskItemMapper,
            IdGenerator idGenerator) {
        this.inventoryMapper = inventoryMapper;
        this.reservationMapper = reservationMapper;
        this.reservationItemMapper = reservationItemMapper;
        this.ledgerMapper = ledgerMapper;
        this.auditLogMapper = auditLogMapper;
        this.auditOutboxMapper = auditOutboxMapper;
        this.auditDeadLetterMapper = auditDeadLetterMapper;
        this.auditReplayTaskMapper = auditReplayTaskMapper;
        this.auditReplayTaskItemMapper = auditReplayTaskItemMapper;
        this.idGenerator = idGenerator;
        log.info("Using MyBatis-Plus inventory repository");
    }

    public Optional<Inventory> findInventory(SkuId skuId) {
        return Optional.ofNullable(inventoryMapper.selectOne(Wrappers.<InventoryDO>lambdaQuery()
                        .eq(InventoryDO::getSkuId, skuId == null ? null : skuId.value())))
                .map(InventoryPersistenceAssembler::toDomain);
    }

    public List<Inventory> findInventories() {
        return inventoryMapper
                .selectList(Wrappers.<InventoryDO>lambdaQuery()
                        .orderByAsc(InventoryDO::getSkuId))
                .stream()
                .map(InventoryPersistenceAssembler::toDomain)
                .toList();
    }

    public List<Inventory> findInventories(Set<SkuId> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        List<Long> skuIdValues = skuIds.stream()
                .filter(java.util.Objects::nonNull)
                .map(SkuId::value)
                .toList();
        if (skuIdValues.isEmpty()) {
            return List.of();
        }
        return inventoryMapper
                .selectList(Wrappers.<InventoryDO>lambdaQuery()
                        .in(InventoryDO::getSkuId, skuIdValues)
                        .orderByAsc(InventoryDO::getSkuId))
                .stream()
                .map(InventoryPersistenceAssembler::toDomain)
                .toList();
    }

    public List<Inventory> pageInventories(SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
        long offset = (long) (pageNo - 1) * pageSize;
        return inventoryMapper
                .selectPageByCondition(
                        skuId == null ? null : skuId.value(),
                        status == null ? null : status.value(),
                        offset,
                        pageSize)
                .stream()
                .map(InventoryPersistenceAssembler::toDomain)
                .toList();
    }

    public long countInventories(SkuId skuId, InventoryStatus status) {
        return inventoryMapper.countByCondition(
                skuId == null ? null : skuId.value(), status == null ? null : status.value());
    }

    public Inventory saveInventory(Inventory inventory) {
        InventoryDO dataObject = InventoryPersistenceAssembler.toDataObject(inventory);
        if (dataObject.getId() == null) {
            inventoryMapper.insert(dataObject);
        } else {
            if (inventoryMapper.updateById(dataObject) == 0) {
                throw new InventoryDomainException(
                        InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED, String.valueOf(inventory.getSkuId()));
            }
        }
        return InventoryPersistenceAssembler.toDomain(dataObject);
    }

    public InventoryReservation saveReservation(InventoryReservation reservation) {
        Long tenantId = currentTenantId();
        InventoryReservationDO reservationDataObject =
                InventoryReservationPersistenceAssembler.toDataObject(reservation);
        if (reservationDataObject.getId() == null) {
            reservationMapper.insert(reservationDataObject);
            List<InventoryReservationItemDO> itemDataObjects = reservation.getItems().stream()
                    .map(item -> InventoryReservationItemPersistenceAssembler.toDataObject(
                            item,
                            tenantId,
                            reservation.getReservationNo() == null
                                    ? null
                                    : reservation.getReservationNo().value()))
                    .toList();
            itemDataObjects.forEach(reservationItemMapper::insert);
        } else {
            reservationMapper.updateById(reservationDataObject);
        }
        return findReservation(reservation.getOrderNo())
                .orElseThrow();
    }

    public Optional<InventoryReservation> findReservation(OrderNo orderNo) {
        Long tenantId = currentTenantId();
        InventoryReservationDO reservation = reservationMapper.selectOne(Wrappers.<InventoryReservationDO>lambdaQuery()
                .eq(InventoryReservationDO::getTenantId, tenantId)
                .eq(InventoryReservationDO::getOrderNo, orderNo == null ? null : orderNo.value()));
        if (reservation == null) {
            return Optional.empty();
        }
        List<InventoryReservationItem> items = reservationItemMapper
                .selectList(Wrappers.<InventoryReservationItemDO>lambdaQuery()
                        .eq(InventoryReservationItemDO::getTenantId, tenantId)
                        .eq(InventoryReservationItemDO::getReservationNo, reservation.getReservationNo())
                        .orderByAsc(InventoryReservationItemDO::getSkuId))
                .stream()
                .map(InventoryReservationItemPersistenceAssembler::toDomain)
                .toList();
        return Optional.of(InventoryReservationPersistenceAssembler.toDomain(reservation, items));
    }

    public void saveLedger(InventoryLedger ledger) {
        ledgerMapper.insert(InventoryLedgerPersistenceAssembler.toDataObject(ledger));
    }

    public List<InventoryLedger> findLedgers(OrderNo orderNo) {
        Long tenantId = currentTenantId();
        return ledgerMapper
                .selectList(Wrappers.<InventoryLedgerDO>lambdaQuery()
                        .eq(InventoryLedgerDO::getTenantId, tenantId)
                        .eq(InventoryLedgerDO::getOrderNo, orderNo == null ? null : orderNo.value())
                        .orderByAsc(InventoryLedgerDO::getOccurredAt, InventoryLedgerDO::getId))
                .stream()
                .map(InventoryLedgerPersistenceAssembler::toDomain)
                .toList();
    }

    public void saveAuditLog(InventoryAuditLog auditLog) {
        auditLogMapper.insert(InventoryAuditLogPersistenceAssembler.toDataObject(auditLog));
    }

    public List<InventoryAuditLog> findAuditLogs(OrderNo orderNo) {
        Long tenantId = currentTenantId();
        return auditLogMapper
                .selectList(Wrappers.<InventoryAuditLogDO>lambdaQuery()
                        .eq(InventoryAuditLogDO::getTenantId, tenantId)
                        .eq(InventoryAuditLogDO::getOrderNo, orderNo == null ? null : orderNo.value())
                        .orderByAsc(InventoryAuditLogDO::getOccurredAt, InventoryAuditLogDO::getId))
                .stream()
                .map(InventoryAuditLogPersistenceAssembler::toDomain)
                .toList();
    }

    public void saveAuditOutbox(InventoryAuditOutbox outbox) {
        InventoryAuditOutboxDO dataObject = InventoryAuditOutboxPersistenceAssembler.toDataObject(outbox);
        if (dataObject.getEventCode() == null || dataObject.getEventCode().isBlank()) {
            dataObject.setEventCode(generateEventCode().value());
        }
        auditOutboxMapper.insert(dataObject);
        outbox.setId(toDomainOutboxId(dataObject.getId()));
        outbox.setEventCode(toDomainEventCode(dataObject.getEventCode()));
    }

    public List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
        return auditOutboxMapper
                .selectList(Wrappers.<InventoryAuditOutboxDO>lambdaQuery()
                        .in(
                                InventoryAuditOutboxDO::getStatus,
                                InventoryAuditOutboxStatus.NEW.value(),
                                InventoryAuditOutboxStatus.RETRYING.value())
                        .and(wrapper -> wrapper.isNull(InventoryAuditOutboxDO::getNextRetryAt)
                                .or()
                                .le(InventoryAuditOutboxDO::getNextRetryAt, now))
                        .orderByAsc(InventoryAuditOutboxDO::getFailedAt, InventoryAuditOutboxDO::getId)
                        .last("limit " + limit))
                .stream()
                .map(InventoryAuditOutboxPersistenceAssembler::toDomain)
                .toList();
    }

    public List<InventoryAuditOutboxRepository.TenantScopedAuditOutbox> claimRetryableAuditOutbox(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        List<InventoryAuditOutboxDO> candidates = auditOutboxMapper
                .selectList(Wrappers.<InventoryAuditOutboxDO>lambdaQuery()
                        .in(
                                InventoryAuditOutboxDO::getStatus,
                                InventoryAuditOutboxStatus.NEW.value(),
                                InventoryAuditOutboxStatus.RETRYING.value())
                        .and(wrapper -> wrapper.isNull(InventoryAuditOutboxDO::getNextRetryAt)
                                .or()
                                .le(InventoryAuditOutboxDO::getNextRetryAt, now))
                        .orderByAsc(InventoryAuditOutboxDO::getFailedAt, InventoryAuditOutboxDO::getId)
                        .last("limit " + Math.max(limit * 3, limit)))
                .stream()
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }
        List<InventoryAuditOutboxRepository.TenantScopedAuditOutbox> claimed = new java.util.ArrayList<>(limit);
        for (InventoryAuditOutboxDO candidate : candidates) {
            if (claimed.size() >= limit) {
                break;
            }
            int updated = auditOutboxMapper.update(
                    null,
                    Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                            .eq(InventoryAuditOutboxDO::getId, candidate.getId())
                            .in(
                                    InventoryAuditOutboxDO::getStatus,
                                    InventoryAuditOutboxStatus.NEW.value(),
                                    InventoryAuditOutboxStatus.RETRYING.value())
                            .and(wrapper -> wrapper.isNull(InventoryAuditOutboxDO::getNextRetryAt)
                                    .or()
                                    .le(InventoryAuditOutboxDO::getNextRetryAt, now))
                            .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.PROCESSING.value())
                            .set(InventoryAuditOutboxDO::getProcessingOwner, processingOwner)
                            .set(InventoryAuditOutboxDO::getLeaseUntil, leaseUntil)
                            .set(InventoryAuditOutboxDO::getClaimedAt, now)
                            .set(InventoryAuditOutboxDO::getUpdatedAt, now));
            if (updated == 0) {
                continue;
            }
            InventoryAuditOutboxDO claimedDataObject = auditOutboxMapper.selectById(candidate.getId());
            if (claimedDataObject != null) {
                claimed.add(new InventoryAuditOutboxRepository.TenantScopedAuditOutbox(
                        claimedDataObject.getTenantId() == null
                                ? null
                                : com.github.thundax.bacon.common.id.domain.TenantId.of(claimedDataObject.getTenantId()),
                        InventoryAuditOutboxPersistenceAssembler.toDomain(claimedDataObject)));
            }
        }
        return List.copyOf(claimed);
    }

    public int releaseExpiredAuditOutboxLease(Instant now) {
        return auditOutboxMapper.update(
                null,
                Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                        .eq(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.PROCESSING.value())
                        .le(InventoryAuditOutboxDO::getLeaseUntil, now)
                        .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.RETRYING.value())
                        .set(InventoryAuditOutboxDO::getProcessingOwner, null)
                        .set(InventoryAuditOutboxDO::getLeaseUntil, null)
                        .set(InventoryAuditOutboxDO::getClaimedAt, null)
                        .set(InventoryAuditOutboxDO::getUpdatedAt, now));
    }

    public void updateAuditOutboxForRetry(
            OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        auditOutboxMapper.update(
                null,
                Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                        .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                        .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.RETRYING.value())
                        .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                        .set(InventoryAuditOutboxDO::getNextRetryAt, nextRetryAt)
                        .set(InventoryAuditOutboxDO::getErrorMessage, errorMessage)
                        .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt));
    }

    public boolean updateAuditOutboxForRetryClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            Instant updatedAt) {
        return auditOutboxMapper.update(
                        null,
                        Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                                .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                                .eq(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.PROCESSING.value())
                                .eq(InventoryAuditOutboxDO::getProcessingOwner, processingOwner)
                                .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.RETRYING.value())
                                .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                                .set(InventoryAuditOutboxDO::getNextRetryAt, nextRetryAt)
                                .set(InventoryAuditOutboxDO::getErrorMessage, errorMessage)
                                .set(InventoryAuditOutboxDO::getProcessingOwner, null)
                                .set(InventoryAuditOutboxDO::getLeaseUntil, null)
                                .set(InventoryAuditOutboxDO::getClaimedAt, null)
                                .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt))
                > 0;
    }

    public void markAuditOutboxDead(OutboxId outboxId, int retryCount, String deadReason, Instant updatedAt) {
        auditOutboxMapper.update(
                null,
                Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                        .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                        .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.DEAD.value())
                        .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                        .set(InventoryAuditOutboxDO::getDeadReason, deadReason)
                        .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt));
    }

    public boolean markAuditOutboxDeadClaimed(
            OutboxId outboxId, String processingOwner, int retryCount, String deadReason, Instant updatedAt) {
        return auditOutboxMapper.update(
                        null,
                        Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                                .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                                .eq(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.PROCESSING.value())
                                .eq(InventoryAuditOutboxDO::getProcessingOwner, processingOwner)
                                .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.DEAD.value())
                                .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                                .set(InventoryAuditOutboxDO::getDeadReason, deadReason)
                                .set(InventoryAuditOutboxDO::getProcessingOwner, null)
                                .set(InventoryAuditOutboxDO::getLeaseUntil, null)
                                .set(InventoryAuditOutboxDO::getClaimedAt, null)
                                .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt))
                > 0;
    }

    public void deleteAuditOutbox(OutboxId outboxId) {
        auditOutboxMapper.deleteById(toDatabaseOutboxId(outboxId));
    }

    public boolean deleteAuditOutboxClaimed(OutboxId outboxId, String processingOwner) {
        return auditOutboxMapper.delete(Wrappers.<InventoryAuditOutboxDO>lambdaQuery()
                        .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                        .eq(InventoryAuditOutboxDO::getStatus, InventoryAuditOutboxStatus.PROCESSING.value())
                        .eq(InventoryAuditOutboxDO::getProcessingOwner, processingOwner))
                > 0;
    }

    public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
        auditDeadLetterMapper.insert(InventoryAuditDeadLetterPersistenceAssembler.toDataObject(deadLetter));
    }

    public List<InventoryAuditDeadLetter> pageAuditDeadLetters(
            OrderNo orderNo, InventoryAuditReplayStatus replayStatus, int pageNo, int pageSize) {
        Long tenantId = currentTenantId();
        long offset = (long) (pageNo - 1) * pageSize;
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InventoryAuditDeadLetterDO> query =
                Wrappers.<InventoryAuditDeadLetterDO>lambdaQuery()
                        .eq(InventoryAuditDeadLetterDO::getTenantId, tenantId);
        if (orderNo != null) {
            query.eq(InventoryAuditDeadLetterDO::getOrderNo, orderNo.value());
        }
        if (replayStatus != null) {
            query.eq(InventoryAuditDeadLetterDO::getReplayStatus, replayStatus.value());
        }
        return auditDeadLetterMapper
                .selectList(query.orderByDesc(
                                InventoryAuditDeadLetterDO::getDeadAt, InventoryAuditDeadLetterDO::getOutboxId)
                        .last("limit " + offset + ", " + pageSize))
                .stream()
                .map(InventoryAuditDeadLetterPersistenceAssembler::toDomain)
                .toList();
    }

    public long countAuditDeadLetters(OrderNo orderNo, InventoryAuditReplayStatus replayStatus) {
        Long tenantId = currentTenantId();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InventoryAuditDeadLetterDO> query =
                Wrappers.<InventoryAuditDeadLetterDO>lambdaQuery()
                        .eq(InventoryAuditDeadLetterDO::getTenantId, tenantId);
        if (orderNo != null) {
            query.eq(InventoryAuditDeadLetterDO::getOrderNo, orderNo.value());
        }
        if (replayStatus != null) {
            query.eq(InventoryAuditDeadLetterDO::getReplayStatus, replayStatus.value());
        }
        return auditDeadLetterMapper.selectCount(query);
    }

    public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(DeadLetterId id) {
        Long tenantId = currentTenantId();
        return Optional.ofNullable(auditDeadLetterMapper.selectOne(Wrappers.<InventoryAuditDeadLetterDO>lambdaQuery()
                        .eq(InventoryAuditDeadLetterDO::getOutboxId, id == null ? null : id.value())
                        .eq(InventoryAuditDeadLetterDO::getTenantId, tenantId)))
                .map(InventoryAuditDeadLetterPersistenceAssembler::toDomain);
    }

    public boolean claimAuditDeadLetterForReplay(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        Long tenantId = currentTenantId();
        return auditDeadLetterMapper.update(
                        null,
                        Wrappers.<InventoryAuditDeadLetterDO>lambdaUpdate()
                                .eq(InventoryAuditDeadLetterDO::getOutboxId, id == null ? null : id.value())
                                .eq(InventoryAuditDeadLetterDO::getTenantId, tenantId)
                                .in(
                                        InventoryAuditDeadLetterDO::getReplayStatus,
                                        InventoryAuditReplayStatus.PENDING.value(),
                                        InventoryAuditReplayStatus.FAILED.value())
                                .set(
                                        InventoryAuditDeadLetterDO::getReplayStatus,
                                        InventoryAuditReplayStatus.RUNNING.value())
                                .set(InventoryAuditDeadLetterDO::getReplayKey, replayKey)
                                .set(
                                        InventoryAuditDeadLetterDO::getReplayOperatorType,
                                        operatorType == null ? null : operatorType.value())
                                .set(
                                        InventoryAuditDeadLetterDO::getReplayOperatorId,
                                        operatorId == null ? null : Long.valueOf(operatorId.value()))
                                .set(InventoryAuditDeadLetterDO::getLastReplayAt, replayAt)
                                .set(InventoryAuditDeadLetterDO::getLastReplayMessage, "RUNNING")
                                .set(InventoryAuditDeadLetterDO::getLastReplayError, null))
                > 0;
    }

    public void markAuditDeadLetterReplaySuccess(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        auditDeadLetterMapper.update(
                null,
                Wrappers.<InventoryAuditDeadLetterDO>lambdaUpdate()
                        .eq(InventoryAuditDeadLetterDO::getOutboxId, id == null ? null : id.value())
                        .set(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditReplayStatus.SUCCEEDED.value())
                        .setSql("replay_count = ifnull(replay_count, 0) + 1")
                        .set(InventoryAuditDeadLetterDO::getReplayKey, replayKey)
                        .set(
                                InventoryAuditDeadLetterDO::getReplayOperatorType,
                                operatorType == null ? null : operatorType.value())
                        .set(
                                InventoryAuditDeadLetterDO::getReplayOperatorId,
                                operatorId == null ? null : Long.valueOf(operatorId.value()))
                        .set(InventoryAuditDeadLetterDO::getLastReplayAt, replayAt)
                        .set(InventoryAuditDeadLetterDO::getLastReplayMessage, "SUCCEEDED")
                        .set(InventoryAuditDeadLetterDO::getLastReplayError, null));
    }

    public void markAuditDeadLetterReplayFailed(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            String replayError,
            Instant replayAt) {
        auditDeadLetterMapper.update(
                null,
                Wrappers.<InventoryAuditDeadLetterDO>lambdaUpdate()
                        .eq(InventoryAuditDeadLetterDO::getOutboxId, id == null ? null : id.value())
                        .set(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditReplayStatus.FAILED.value())
                        .setSql("replay_count = ifnull(replay_count, 0) + 1")
                        .set(InventoryAuditDeadLetterDO::getReplayKey, replayKey)
                        .set(
                                InventoryAuditDeadLetterDO::getReplayOperatorType,
                                operatorType == null ? null : operatorType.value())
                        .set(
                                InventoryAuditDeadLetterDO::getReplayOperatorId,
                                operatorId == null ? null : Long.valueOf(operatorId.value()))
                        .set(InventoryAuditDeadLetterDO::getLastReplayAt, replayAt)
                        .set(InventoryAuditDeadLetterDO::getLastReplayMessage, "FAILED")
                        .set(InventoryAuditDeadLetterDO::getLastReplayError, replayError));
    }

    private Long currentTenantId() {
        return BaconIdContextHelper.requireTenantId().value();
    }

    public InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
        InventoryAuditReplayTaskDO dataObject =
                InventoryAuditReplayTaskPersistenceAssembler.toDataObject(currentTenantId(), task);
        if (dataObject.getId() == null) {
            auditReplayTaskMapper.insert(dataObject);
        } else {
            auditReplayTaskMapper.updateById(dataObject);
        }
        return InventoryAuditReplayTaskPersistenceAssembler.toDomain(dataObject);
    }

    public void batchSaveAuditReplayTaskItems(TaskId taskId, List<DeadLetterId> deadLetterIds, Instant createdAt) {
        Long tenantId = currentTenantId();
        if (deadLetterIds == null || deadLetterIds.isEmpty()) {
            return;
        }
        for (DeadLetterId deadLetterId : deadLetterIds) {
            auditReplayTaskItemMapper.insert(new InventoryAuditReplayTaskItemDO(
                    null,
                    taskId == null ? null : taskId.value(),
                    tenantId,
                    deadLetterId == null ? null : deadLetterId.value(),
                    InventoryAuditReplayTaskItemStatus.PENDING.value(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    createdAt));
        }
    }

    public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(TaskId taskId) {
        return Optional.ofNullable(auditReplayTaskMapper.selectById(taskId == null ? null : taskId.value()))
                .map(InventoryAuditReplayTaskPersistenceAssembler::toDomain);
    }

    public Long findAuditReplayTaskTenantId(TaskId taskId) {
        InventoryAuditReplayTaskDO dataObject = auditReplayTaskMapper.selectById(taskId == null ? null : taskId.value());
        return dataObject == null ? null : dataObject.getTenantId();
    }

    public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        List<InventoryAuditReplayTaskDO> candidates =
                auditReplayTaskMapper.selectList(Wrappers.<InventoryAuditReplayTaskDO>lambdaQuery()
                        .in(
                                InventoryAuditReplayTaskDO::getStatus,
                                InventoryAuditReplayTaskStatus.PENDING,
                                InventoryAuditReplayTaskStatus.RUNNING)
                        .and(wrapper -> wrapper.isNull(InventoryAuditReplayTaskDO::getLeaseUntil)
                                .or()
                                .le(InventoryAuditReplayTaskDO::getLeaseUntil, now))
                        .orderByAsc(InventoryAuditReplayTaskDO::getCreatedAt, InventoryAuditReplayTaskDO::getId)
                        .last("limit " + Math.max(limit * 3, limit)));
        if (candidates.isEmpty()) {
            return List.of();
        }
        List<InventoryAuditReplayTask> claimed = new java.util.ArrayList<>(limit);
        for (InventoryAuditReplayTaskDO candidate : candidates) {
            if (claimed.size() >= limit) {
                break;
            }
            int updated = auditReplayTaskMapper.update(
                    null,
                    Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                            .eq(InventoryAuditReplayTaskDO::getId, candidate.getId())
                            .in(
                                    InventoryAuditReplayTaskDO::getStatus,
                                    InventoryAuditReplayTaskStatus.PENDING,
                                    InventoryAuditReplayTaskStatus.RUNNING)
                            .and(wrapper -> wrapper.isNull(InventoryAuditReplayTaskDO::getLeaseUntil)
                                    .or()
                                    .le(InventoryAuditReplayTaskDO::getLeaseUntil, now))
                            .set(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTaskStatus.RUNNING)
                            .set(InventoryAuditReplayTaskDO::getProcessingOwner, processingOwner)
                            .set(InventoryAuditReplayTaskDO::getLeaseUntil, leaseUntil)
                            .set(
                                    InventoryAuditReplayTaskDO::getStartedAt,
                                    candidate.getStartedAt() == null ? now : candidate.getStartedAt())
                            .set(InventoryAuditReplayTaskDO::getUpdatedAt, now));
            if (updated == 0) {
                continue;
            }
            InventoryAuditReplayTaskDO claimedDataObject = auditReplayTaskMapper.selectById(candidate.getId());
            if (claimedDataObject != null) {
                claimed.add(InventoryAuditReplayTaskPersistenceAssembler.toDomain(claimedDataObject));
            }
        }
        return List.copyOf(claimed);
    }

    public void renewAuditReplayTaskLease(
            TaskId taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
        auditReplayTaskMapper.update(
                null,
                Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                        .eq(InventoryAuditReplayTaskDO::getId, taskId == null ? null : taskId.value())
                        .eq(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTaskStatus.RUNNING)
                        .eq(InventoryAuditReplayTaskDO::getProcessingOwner, processingOwner)
                        .set(InventoryAuditReplayTaskDO::getLeaseUntil, leaseUntil)
                        .set(InventoryAuditReplayTaskDO::getUpdatedAt, updatedAt));
    }

    public List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(TaskId taskId, int limit) {
        return auditReplayTaskItemMapper
                .selectList(Wrappers.<InventoryAuditReplayTaskItemDO>lambdaQuery()
                        .eq(InventoryAuditReplayTaskItemDO::getTaskId, taskId == null ? null : taskId.value())
                        .eq(
                                InventoryAuditReplayTaskItemDO::getItemStatus,
                                InventoryAuditReplayTaskItemStatus.PENDING.value())
                        .orderByAsc(InventoryAuditReplayTaskItemDO::getId)
                        .last("limit " + limit))
                .stream()
                .map(InventoryAuditReplayTaskItemPersistenceAssembler::toDomain)
                .toList();
    }

    public void markAuditReplayTaskItemResult(
            Long itemId,
            InventoryAuditReplayTaskItemStatus itemStatus,
            InventoryAuditReplayStatus replayStatus,
            String replayKey,
            String resultMessage,
            Instant startedAt,
            Instant finishedAt) {
        auditReplayTaskItemMapper.update(
                null,
                Wrappers.<InventoryAuditReplayTaskItemDO>lambdaUpdate()
                        .eq(InventoryAuditReplayTaskItemDO::getId, itemId)
                        .eq(
                                InventoryAuditReplayTaskItemDO::getItemStatus,
                                InventoryAuditReplayTaskItemStatus.PENDING.value())
                        .set(InventoryAuditReplayTaskItemDO::getItemStatus, itemStatus.value())
                        .set(
                                InventoryAuditReplayTaskItemDO::getReplayStatus,
                                replayStatus == null ? null : replayStatus.value())
                        .set(InventoryAuditReplayTaskItemDO::getReplayKey, replayKey)
                        .set(InventoryAuditReplayTaskItemDO::getResultMessage, resultMessage)
                        .set(InventoryAuditReplayTaskItemDO::getStartedAt, startedAt)
                        .set(InventoryAuditReplayTaskItemDO::getFinishedAt, finishedAt)
                        .set(InventoryAuditReplayTaskItemDO::getUpdatedAt, finishedAt));
    }

    public void incrementAuditReplayTaskProgress(
            TaskId taskId,
            String processingOwner,
            int processedDelta,
            int successDelta,
            int failedDelta,
            Instant updatedAt) {
        auditReplayTaskMapper.update(
                null,
                Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                        .eq(InventoryAuditReplayTaskDO::getId, taskId == null ? null : taskId.value())
                        .eq(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTaskStatus.RUNNING)
                        .eq(InventoryAuditReplayTaskDO::getProcessingOwner, processingOwner)
                        .setSql("processed_count = ifnull(processed_count, 0) + " + Math.max(processedDelta, 0))
                        .setSql("success_count = ifnull(success_count, 0) + " + Math.max(successDelta, 0))
                        .setSql("failed_count = ifnull(failed_count, 0) + " + Math.max(failedDelta, 0))
                        .set(InventoryAuditReplayTaskDO::getUpdatedAt, updatedAt));
    }

    public void finishAuditReplayTask(
            TaskId taskId, String processingOwner, String status, String lastError, Instant finishedAt) {
        auditReplayTaskMapper.update(
                null,
                Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                        .eq(InventoryAuditReplayTaskDO::getId, taskId == null ? null : taskId.value())
                        .eq(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTaskStatus.RUNNING)
                        .eq(InventoryAuditReplayTaskDO::getProcessingOwner, processingOwner)
                        .set(InventoryAuditReplayTaskDO::getStatus, status)
                        .set(InventoryAuditReplayTaskDO::getLastError, lastError)
                        .set(InventoryAuditReplayTaskDO::getProcessingOwner, null)
                        .set(InventoryAuditReplayTaskDO::getLeaseUntil, null)
                        .set(InventoryAuditReplayTaskDO::getFinishedAt, finishedAt)
                        .set(InventoryAuditReplayTaskDO::getUpdatedAt, finishedAt));
    }

    public boolean pauseAuditReplayTask(TaskId taskId, OperatorId operatorId, Instant pausedAt) {
        Long tenantId = currentTenantId();
        return auditReplayTaskMapper.update(
                        null,
                        Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                                .eq(InventoryAuditReplayTaskDO::getId, taskId == null ? null : taskId.value())
                                .eq(InventoryAuditReplayTaskDO::getTenantId, tenantId)
                                .in(
                                        InventoryAuditReplayTaskDO::getStatus,
                                        InventoryAuditReplayTaskStatus.PENDING,
                                        InventoryAuditReplayTaskStatus.RUNNING)
                                .set(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTaskStatus.PAUSED)
                                .set(InventoryAuditReplayTaskDO::getProcessingOwner, null)
                                .set(InventoryAuditReplayTaskDO::getLeaseUntil, null)
                                .set(InventoryAuditReplayTaskDO::getPausedAt, pausedAt)
                                .set(InventoryAuditReplayTaskDO::getUpdatedAt, pausedAt))
                > 0;
    }

    public boolean resumeAuditReplayTask(TaskId taskId, OperatorId operatorId, Instant updatedAt) {
        Long tenantId = currentTenantId();
        return auditReplayTaskMapper.update(
                        null,
                        Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                                .eq(InventoryAuditReplayTaskDO::getId, taskId == null ? null : taskId.value())
                                .eq(InventoryAuditReplayTaskDO::getTenantId, tenantId)
                                .eq(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTaskStatus.PAUSED)
                                .set(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTaskStatus.PENDING)
                                .set(InventoryAuditReplayTaskDO::getPausedAt, null)
                                .set(InventoryAuditReplayTaskDO::getProcessingOwner, null)
                                .set(InventoryAuditReplayTaskDO::getLeaseUntil, null)
                                .set(InventoryAuditReplayTaskDO::getUpdatedAt, updatedAt))
                > 0;
    }

    private EventCode generateEventCode() {
        long id = idGenerator.nextId(AUDIT_OUTBOX_EVENT_CODE_BIZ_TAG);
        String timestamp = LocalDateTime.now().format(EVENT_CODE_TIMESTAMP_FORMATTER);
        String suffix = String.format("%06d", Math.floorMod(id, 1_000_000L));
        return EventCode.of("EVT" + timestamp + "-" + suffix);
    }

    private Long toDatabaseOutboxId(OutboxId outboxId) {
        return outboxId == null ? null : outboxId.value();
    }

    private OutboxId toDomainOutboxId(Long outboxId) {
        return outboxId == null ? null : OutboxId.of(outboxId);
    }

    private EventCode toDomainEventCode(String eventCode) {
        return eventCode == null ? null : EventCode.of(eventCode);
    }
}
