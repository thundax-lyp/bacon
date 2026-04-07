package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.WarehouseNo;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditLogDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditDeadLetterDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditOutboxDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditReplayTaskDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditReplayTaskItemDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryLedgerDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationItemDO;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditLogMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditDeadLetterMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditOutboxMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditReplayTaskMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditReplayTaskItemMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryLedgerMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryReservationItemMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryReservationMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private static final DateTimeFormatter EVENT_CODE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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

    public InventoryRepositorySupport(InventoryMapper inventoryMapper,
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

    public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
        return Optional.ofNullable(inventoryMapper.selectOne(Wrappers.<InventoryDO>lambdaQuery()
                .eq(InventoryDO::getTenantId, tenantId)
                .eq(InventoryDO::getSkuId, skuId)))
                .map(this::toDomain);
    }

    public List<Inventory> findInventories(Long tenantId) {
        return inventoryMapper.selectList(Wrappers.<InventoryDO>lambdaQuery()
                        .eq(InventoryDO::getTenantId, tenantId)
                        .orderByAsc(InventoryDO::getSkuId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        return inventoryMapper.selectList(Wrappers.<InventoryDO>lambdaQuery()
                        .eq(InventoryDO::getTenantId, tenantId)
                        .in(InventoryDO::getSkuId, skuIds)
                        .orderByAsc(InventoryDO::getSkuId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Inventory> pageInventories(Long tenantId, Long skuId, String status, int pageNo, int pageSize) {
        long offset = (long) (pageNo - 1) * pageSize;
        return inventoryMapper.selectPageByCondition(tenantId, skuId, status, offset, pageSize)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public long countInventories(Long tenantId, Long skuId, String status) {
        return inventoryMapper.countByCondition(tenantId, skuId, status);
    }

    public Inventory saveInventory(Inventory inventory) {
        InventoryDO dataObject = toDataObject(inventory);
        if (dataObject.getId() == null) {
            inventoryMapper.insert(dataObject);
        } else {
            if (inventoryMapper.updateById(dataObject) == 0) {
                throw new InventoryDomainException(InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED,
                        String.valueOf(inventory.getSkuId()));
            }
        }
        return toDomain(dataObject);
    }

    public InventoryReservation saveReservation(InventoryReservation reservation) {
        InventoryReservationDO reservationDataObject = toDataObject(reservation);
        if (reservationDataObject.getId() == null) {
            reservationMapper.insert(reservationDataObject);
            List<InventoryReservationItemDO> itemDataObjects = reservation.getItems().stream()
                    .map(item -> toDataObject(item, reservation.getTenantId(), reservation.getReservationNo()))
                    .toList();
            itemDataObjects.forEach(reservationItemMapper::insert);
        } else {
            reservationMapper.updateById(reservationDataObject);
        }
        return findReservation(reservation.getTenantId(), reservation.getOrderNo()).orElseThrow();
    }

    public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
        InventoryReservationDO reservation = reservationMapper.selectOne(
                Wrappers.<InventoryReservationDO>lambdaQuery()
                        .eq(InventoryReservationDO::getTenantId, tenantId)
                        .eq(InventoryReservationDO::getOrderNo, orderNo));
        if (reservation == null) {
            return Optional.empty();
        }
        List<InventoryReservationItem> items = reservationItemMapper.selectList(
                        Wrappers.<InventoryReservationItemDO>lambdaQuery()
                                .eq(InventoryReservationItemDO::getTenantId, tenantId)
                                .eq(InventoryReservationItemDO::getReservationNo, reservation.getReservationNo())
                                .orderByAsc(InventoryReservationItemDO::getSkuId))
                .stream()
                .map(this::toDomain)
                .toList();
        return Optional.of(toDomain(reservation, items));
    }

    public void saveLedger(InventoryLedger ledger) {
        ledgerMapper.insert(toDataObject(ledger));
    }

    public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
        return ledgerMapper.selectList(Wrappers.<InventoryLedgerDO>lambdaQuery()
                        .eq(InventoryLedgerDO::getTenantId, tenantId)
                        .eq(InventoryLedgerDO::getOrderNo, orderNo)
                        .orderByAsc(InventoryLedgerDO::getOccurredAt, InventoryLedgerDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public void saveAuditLog(InventoryAuditLog auditLog) {
        auditLogMapper.insert(toDataObject(auditLog));
    }

    public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return auditLogMapper.selectList(Wrappers.<InventoryAuditLogDO>lambdaQuery()
                        .eq(InventoryAuditLogDO::getTenantId, tenantId)
                        .eq(InventoryAuditLogDO::getOrderNo, orderNo)
                        .orderByAsc(InventoryAuditLogDO::getOccurredAt, InventoryAuditLogDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public void saveAuditOutbox(InventoryAuditOutbox outbox) {
        InventoryAuditOutboxDO dataObject = toDataObject(outbox);
        if (dataObject.getEventCode() == null || dataObject.getEventCode().isBlank()) {
            dataObject.setEventCode(generateEventCode().value());
        }
        auditOutboxMapper.insert(dataObject);
        outbox.setId(toDomainOutboxId(dataObject.getId()));
        outbox.setEventCode(toDomainEventCode(dataObject.getEventCode()));
    }

    public List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
        return auditOutboxMapper.selectList(Wrappers.<InventoryAuditOutboxDO>lambdaQuery()
                        .in(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_NEW,
                                InventoryAuditOutbox.STATUS_RETRYING)
                        .and(wrapper -> wrapper.isNull(InventoryAuditOutboxDO::getNextRetryAt)
                                .or()
                                .le(InventoryAuditOutboxDO::getNextRetryAt, now))
                        .orderByAsc(InventoryAuditOutboxDO::getFailedAt, InventoryAuditOutboxDO::getId)
                        .last("limit " + limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public List<InventoryAuditOutbox> claimRetryableAuditOutbox(Instant now, int limit,
                                                                 String processingOwner, Instant leaseUntil) {
        List<InventoryAuditOutboxDO> candidates = auditOutboxMapper.selectList(Wrappers.<InventoryAuditOutboxDO>lambdaQuery()
                        .in(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_NEW,
                                InventoryAuditOutbox.STATUS_RETRYING)
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
        List<InventoryAuditOutbox> claimed = new java.util.ArrayList<>(limit);
        for (InventoryAuditOutboxDO candidate : candidates) {
            if (claimed.size() >= limit) {
                break;
            }
            int updated = auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                    .eq(InventoryAuditOutboxDO::getId, candidate.getId())
                    .in(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_NEW, InventoryAuditOutbox.STATUS_RETRYING)
                    .and(wrapper -> wrapper.isNull(InventoryAuditOutboxDO::getNextRetryAt)
                            .or()
                            .le(InventoryAuditOutboxDO::getNextRetryAt, now))
                    .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_PROCESSING)
                    .set(InventoryAuditOutboxDO::getProcessingOwner, processingOwner)
                    .set(InventoryAuditOutboxDO::getLeaseUntil, leaseUntil)
                    .set(InventoryAuditOutboxDO::getClaimedAt, now)
                    .set(InventoryAuditOutboxDO::getUpdatedAt, now));
            if (updated == 0) {
                continue;
            }
            InventoryAuditOutboxDO claimedDataObject = auditOutboxMapper.selectById(candidate.getId());
            if (claimedDataObject != null) {
                claimed.add(toDomain(claimedDataObject));
            }
        }
        return List.copyOf(claimed);
    }

    public int releaseExpiredAuditOutboxLease(Instant now) {
        return auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                .eq(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_PROCESSING)
                .le(InventoryAuditOutboxDO::getLeaseUntil, now)
                .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_RETRYING)
                .set(InventoryAuditOutboxDO::getProcessingOwner, null)
                .set(InventoryAuditOutboxDO::getLeaseUntil, null)
                .set(InventoryAuditOutboxDO::getClaimedAt, null)
                .set(InventoryAuditOutboxDO::getUpdatedAt, now));
    }

    public void updateAuditOutboxForRetry(OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage,
                                          Instant updatedAt) {
        auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_RETRYING)
                .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                .set(InventoryAuditOutboxDO::getNextRetryAt, nextRetryAt)
                .set(InventoryAuditOutboxDO::getErrorMessage, errorMessage)
                .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt));
    }

    public boolean updateAuditOutboxForRetryClaimed(OutboxId outboxId, String processingOwner, int retryCount,
                                                    Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        return auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                .eq(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_PROCESSING)
                .eq(InventoryAuditOutboxDO::getProcessingOwner, processingOwner)
                .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_RETRYING)
                .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                .set(InventoryAuditOutboxDO::getNextRetryAt, nextRetryAt)
                .set(InventoryAuditOutboxDO::getErrorMessage, errorMessage)
                .set(InventoryAuditOutboxDO::getProcessingOwner, null)
                .set(InventoryAuditOutboxDO::getLeaseUntil, null)
                .set(InventoryAuditOutboxDO::getClaimedAt, null)
                .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt)) > 0;
    }

    public void markAuditOutboxDead(OutboxId outboxId, int retryCount, String deadReason, Instant updatedAt) {
        auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_DEAD)
                .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                .set(InventoryAuditOutboxDO::getDeadReason, deadReason)
                .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt));
    }

    public boolean markAuditOutboxDeadClaimed(OutboxId outboxId, String processingOwner, int retryCount,
                                              String deadReason, Instant updatedAt) {
        return auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                .eq(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_PROCESSING)
                .eq(InventoryAuditOutboxDO::getProcessingOwner, processingOwner)
                .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_DEAD)
                .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                .set(InventoryAuditOutboxDO::getDeadReason, deadReason)
                .set(InventoryAuditOutboxDO::getProcessingOwner, null)
                .set(InventoryAuditOutboxDO::getLeaseUntil, null)
                .set(InventoryAuditOutboxDO::getClaimedAt, null)
                .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt)) > 0;
    }

    public void deleteAuditOutbox(OutboxId outboxId) {
        auditOutboxMapper.deleteById(toDatabaseOutboxId(outboxId));
    }

    public boolean deleteAuditOutboxClaimed(OutboxId outboxId, String processingOwner) {
        return auditOutboxMapper.delete(Wrappers.<InventoryAuditOutboxDO>lambdaQuery()
                .eq(InventoryAuditOutboxDO::getId, toDatabaseOutboxId(outboxId))
                .eq(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_PROCESSING)
                .eq(InventoryAuditOutboxDO::getProcessingOwner, processingOwner)) > 0;
    }

    public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
        auditDeadLetterMapper.insert(toDataObject(deadLetter));
    }

    public List<InventoryAuditDeadLetter> pageAuditDeadLetters(Long tenantId, String orderNo,
                                                                String replayStatus, int pageNo, int pageSize) {
        long offset = (long) (pageNo - 1) * pageSize;
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InventoryAuditDeadLetterDO> query =
                Wrappers.<InventoryAuditDeadLetterDO>lambdaQuery()
                        .eq(InventoryAuditDeadLetterDO::getTenantId, tenantId);
        if (orderNo != null && !orderNo.isBlank()) {
            query.eq(InventoryAuditDeadLetterDO::getOrderNo, orderNo);
        }
        if (replayStatus != null && !replayStatus.isBlank()) {
            query.eq(InventoryAuditDeadLetterDO::getReplayStatus, replayStatus);
        }
        return auditDeadLetterMapper.selectList(query
                        .orderByDesc(InventoryAuditDeadLetterDO::getDeadAt, InventoryAuditDeadLetterDO::getOutboxId)
                        .last("limit " + offset + ", " + pageSize))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public long countAuditDeadLetters(Long tenantId, String orderNo, String replayStatus) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InventoryAuditDeadLetterDO> query =
                Wrappers.<InventoryAuditDeadLetterDO>lambdaQuery()
                        .eq(InventoryAuditDeadLetterDO::getTenantId, tenantId);
        if (orderNo != null && !orderNo.isBlank()) {
            query.eq(InventoryAuditDeadLetterDO::getOrderNo, orderNo);
        }
        if (replayStatus != null && !replayStatus.isBlank()) {
            query.eq(InventoryAuditDeadLetterDO::getReplayStatus, replayStatus);
        }
        return auditDeadLetterMapper.selectCount(query);
    }

    public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(Long id) {
        return Optional.ofNullable(auditDeadLetterMapper.selectOne(Wrappers.<InventoryAuditDeadLetterDO>lambdaQuery()
                .eq(InventoryAuditDeadLetterDO::getOutboxId, id)))
                .map(this::toDomain);
    }

    public boolean claimAuditDeadLetterForReplay(Long id, Long tenantId, String replayKey,
                                                 String operatorType, Long operatorId, Instant replayAt) {
        return auditDeadLetterMapper.update(null, Wrappers.<InventoryAuditDeadLetterDO>lambdaUpdate()
                .eq(InventoryAuditDeadLetterDO::getOutboxId, id)
                .eq(InventoryAuditDeadLetterDO::getTenantId, tenantId)
                .in(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditReplayStatus.PENDING.value(),
                        InventoryAuditReplayStatus.FAILED.value())
                .set(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditReplayStatus.RUNNING.value())
                .set(InventoryAuditDeadLetterDO::getReplayKey, replayKey)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorType, operatorType)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorId, operatorId)
                .set(InventoryAuditDeadLetterDO::getLastReplayAt, replayAt)
                .set(InventoryAuditDeadLetterDO::getLastReplayResult, "RUNNING")
                .set(InventoryAuditDeadLetterDO::getLastReplayError, null)) > 0;
    }

    public void markAuditDeadLetterReplaySuccess(Long id, String replayKey, String operatorType, Long operatorId,
                                                 Instant replayAt) {
        auditDeadLetterMapper.update(null, Wrappers.<InventoryAuditDeadLetterDO>lambdaUpdate()
                .eq(InventoryAuditDeadLetterDO::getOutboxId, id)
                .set(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditReplayStatus.SUCCEEDED.value())
                .setSql("replay_count = ifnull(replay_count, 0) + 1")
                .set(InventoryAuditDeadLetterDO::getReplayKey, replayKey)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorType, operatorType)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorId, operatorId)
                .set(InventoryAuditDeadLetterDO::getLastReplayAt, replayAt)
                .set(InventoryAuditDeadLetterDO::getLastReplayResult, "SUCCEEDED")
                .set(InventoryAuditDeadLetterDO::getLastReplayError, null));
    }

    public void markAuditDeadLetterReplayFailed(Long id, String replayKey, String operatorType, Long operatorId,
                                                String replayError, Instant replayAt) {
        auditDeadLetterMapper.update(null, Wrappers.<InventoryAuditDeadLetterDO>lambdaUpdate()
                .eq(InventoryAuditDeadLetterDO::getOutboxId, id)
                .set(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditReplayStatus.FAILED.value())
                .setSql("replay_count = ifnull(replay_count, 0) + 1")
                .set(InventoryAuditDeadLetterDO::getReplayKey, replayKey)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorType, operatorType)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorId, operatorId)
                .set(InventoryAuditDeadLetterDO::getLastReplayAt, replayAt)
                .set(InventoryAuditDeadLetterDO::getLastReplayResult, "FAILED")
                .set(InventoryAuditDeadLetterDO::getLastReplayError, replayError));
    }

    public InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
        InventoryAuditReplayTaskDO dataObject = toDataObject(task);
        if (dataObject.getId() == null) {
            auditReplayTaskMapper.insert(dataObject);
        } else {
            auditReplayTaskMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    public void batchSaveAuditReplayTaskItems(Long taskId, Long tenantId, List<Long> deadLetterIds, Instant createdAt) {
        if (deadLetterIds == null || deadLetterIds.isEmpty()) {
            return;
        }
        for (Long deadLetterId : deadLetterIds) {
            auditReplayTaskItemMapper.insert(new InventoryAuditReplayTaskItemDO(null, taskId, tenantId, deadLetterId,
                    InventoryAuditReplayTaskItem.STATUS_PENDING, null, null, null, null, null, createdAt));
        }
    }

    public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(Long taskId) {
        return Optional.ofNullable(auditReplayTaskMapper.selectById(taskId)).map(this::toDomain);
    }

    public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(Instant now, int limit,
                                                                        String processingOwner, Instant leaseUntil) {
        List<InventoryAuditReplayTaskDO> candidates = auditReplayTaskMapper.selectList(
                Wrappers.<InventoryAuditReplayTaskDO>lambdaQuery()
                        .in(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_PENDING,
                                InventoryAuditReplayTask.STATUS_RUNNING)
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
            int updated = auditReplayTaskMapper.update(null, Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                    .eq(InventoryAuditReplayTaskDO::getId, candidate.getId())
                    .in(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_PENDING,
                            InventoryAuditReplayTask.STATUS_RUNNING)
                    .and(wrapper -> wrapper.isNull(InventoryAuditReplayTaskDO::getLeaseUntil)
                            .or()
                            .le(InventoryAuditReplayTaskDO::getLeaseUntil, now))
                    .set(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_RUNNING)
                    .set(InventoryAuditReplayTaskDO::getProcessingOwner, processingOwner)
                    .set(InventoryAuditReplayTaskDO::getLeaseUntil, leaseUntil)
                    .set(InventoryAuditReplayTaskDO::getStartedAt,
                            candidate.getStartedAt() == null ? now : candidate.getStartedAt())
                    .set(InventoryAuditReplayTaskDO::getUpdatedAt, now));
            if (updated == 0) {
                continue;
            }
            InventoryAuditReplayTaskDO claimedDataObject = auditReplayTaskMapper.selectById(candidate.getId());
            if (claimedDataObject != null) {
                claimed.add(toDomain(claimedDataObject));
            }
        }
        return List.copyOf(claimed);
    }

    public void renewAuditReplayTaskLease(Long taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
        auditReplayTaskMapper.update(null, Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                .eq(InventoryAuditReplayTaskDO::getId, taskId)
                .eq(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_RUNNING)
                .eq(InventoryAuditReplayTaskDO::getProcessingOwner, processingOwner)
                .set(InventoryAuditReplayTaskDO::getLeaseUntil, leaseUntil)
                .set(InventoryAuditReplayTaskDO::getUpdatedAt, updatedAt));
    }

    public List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(Long taskId, int limit) {
        return auditReplayTaskItemMapper.selectList(Wrappers.<InventoryAuditReplayTaskItemDO>lambdaQuery()
                        .eq(InventoryAuditReplayTaskItemDO::getTaskId, taskId)
                        .eq(InventoryAuditReplayTaskItemDO::getItemStatus, InventoryAuditReplayTaskItem.STATUS_PENDING)
                        .orderByAsc(InventoryAuditReplayTaskItemDO::getId)
                        .last("limit " + limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public void markAuditReplayTaskItemResult(Long itemId, String itemStatus, String replayStatus,
                                              String replayKey, String resultMessage, Instant startedAt,
                                              Instant finishedAt) {
        auditReplayTaskItemMapper.update(null, Wrappers.<InventoryAuditReplayTaskItemDO>lambdaUpdate()
                .eq(InventoryAuditReplayTaskItemDO::getId, itemId)
                .eq(InventoryAuditReplayTaskItemDO::getItemStatus, InventoryAuditReplayTaskItem.STATUS_PENDING)
                .set(InventoryAuditReplayTaskItemDO::getItemStatus, itemStatus)
                .set(InventoryAuditReplayTaskItemDO::getReplayStatus, replayStatus)
                .set(InventoryAuditReplayTaskItemDO::getReplayKey, replayKey)
                .set(InventoryAuditReplayTaskItemDO::getResultMessage, resultMessage)
                .set(InventoryAuditReplayTaskItemDO::getStartedAt, startedAt)
                .set(InventoryAuditReplayTaskItemDO::getFinishedAt, finishedAt)
                .set(InventoryAuditReplayTaskItemDO::getUpdatedAt, finishedAt));
    }

    public void incrementAuditReplayTaskProgress(Long taskId, String processingOwner, int processedDelta,
                                                 int successDelta, int failedDelta, Instant updatedAt) {
        auditReplayTaskMapper.update(null, Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                .eq(InventoryAuditReplayTaskDO::getId, taskId)
                .eq(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_RUNNING)
                .eq(InventoryAuditReplayTaskDO::getProcessingOwner, processingOwner)
                .setSql("processed_count = ifnull(processed_count, 0) + " + Math.max(processedDelta, 0))
                .setSql("success_count = ifnull(success_count, 0) + " + Math.max(successDelta, 0))
                .setSql("failed_count = ifnull(failed_count, 0) + " + Math.max(failedDelta, 0))
                .set(InventoryAuditReplayTaskDO::getUpdatedAt, updatedAt));
    }

    public void finishAuditReplayTask(Long taskId, String processingOwner, String status, String lastError,
                                      Instant finishedAt) {
        auditReplayTaskMapper.update(null, Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                .eq(InventoryAuditReplayTaskDO::getId, taskId)
                .eq(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_RUNNING)
                .eq(InventoryAuditReplayTaskDO::getProcessingOwner, processingOwner)
                .set(InventoryAuditReplayTaskDO::getStatus, status)
                .set(InventoryAuditReplayTaskDO::getLastError, lastError)
                .set(InventoryAuditReplayTaskDO::getProcessingOwner, null)
                .set(InventoryAuditReplayTaskDO::getLeaseUntil, null)
                .set(InventoryAuditReplayTaskDO::getFinishedAt, finishedAt)
                .set(InventoryAuditReplayTaskDO::getUpdatedAt, finishedAt));
    }

    public boolean pauseAuditReplayTask(Long taskId, Long tenantId, Long operatorId, Instant pausedAt) {
        return auditReplayTaskMapper.update(null, Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                .eq(InventoryAuditReplayTaskDO::getId, taskId)
                .eq(InventoryAuditReplayTaskDO::getTenantId, tenantId)
                .in(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_PENDING,
                        InventoryAuditReplayTask.STATUS_RUNNING)
                .set(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_PAUSED)
                .set(InventoryAuditReplayTaskDO::getProcessingOwner, null)
                .set(InventoryAuditReplayTaskDO::getLeaseUntil, null)
                .set(InventoryAuditReplayTaskDO::getPausedAt, pausedAt)
                .set(InventoryAuditReplayTaskDO::getUpdatedAt, pausedAt)) > 0;
    }

    public boolean resumeAuditReplayTask(Long taskId, Long tenantId, Long operatorId, Instant updatedAt) {
        return auditReplayTaskMapper.update(null, Wrappers.<InventoryAuditReplayTaskDO>lambdaUpdate()
                .eq(InventoryAuditReplayTaskDO::getId, taskId)
                .eq(InventoryAuditReplayTaskDO::getTenantId, tenantId)
                .eq(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_PAUSED)
                .set(InventoryAuditReplayTaskDO::getStatus, InventoryAuditReplayTask.STATUS_PENDING)
                .set(InventoryAuditReplayTaskDO::getPausedAt, null)
                .set(InventoryAuditReplayTaskDO::getProcessingOwner, null)
                .set(InventoryAuditReplayTaskDO::getLeaseUntil, null)
                .set(InventoryAuditReplayTaskDO::getUpdatedAt, updatedAt)) > 0;
    }

    private Inventory toDomain(InventoryDO dataObject) {
        return new Inventory(dataObject.getId(), dataObject.getTenantId(), dataObject.getSkuId(), dataObject.getWarehouseNo(),
                dataObject.getOnHandQuantity(), dataObject.getReservedQuantity(), dataObject.getAvailableQuantity(),
                InventoryStatus.fromValue(dataObject.getStatus()), dataObject.getVersion(),
                dataObject.getUpdatedAt() == null ? dataObject.getCreatedAt() : dataObject.getUpdatedAt());
    }

    private InventoryDO toDataObject(Inventory inventory) {
        return new InventoryDO(inventory.getId() == null ? null : inventory.getId().getIdValue(),
                inventory.getTenantIdValue(), inventory.getSkuIdValue(),
                inventory.getWarehouseNoValue(), inventory.getOnHandQuantity(), inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(), inventory.getStatus().value(), inventory.getVersion(), null,
                inventory.getUpdatedAt(), null,
                inventory.getUpdatedAt());
    }

    private InventoryReservation toDomain(InventoryReservationDO reservation, List<InventoryReservationItem> items) {
        return InventoryReservation.rehydrate(reservation.getId(), reservation.getTenantId(),
                reservation.getReservationNo(), reservation.getOrderNo(),
                reservation.getWarehouseNo(),
                reservation.getCreatedAt(), items, reservation.getReservationStatus(), reservation.getFailureReason(),
                reservation.getReleaseReason(), reservation.getReleasedAt(), reservation.getDeductedAt());
    }

    private InventoryReservationItem toDomain(InventoryReservationItemDO item) {
        return new InventoryReservationItem(item.getId(), item.getTenantId(), item.getReservationNo(), item.getSkuId(),
                item.getQuantity());
    }

    private InventoryReservationDO toDataObject(InventoryReservation reservation) {
        return new InventoryReservationDO(reservation.getId(), reservation.getTenantId(),
                reservation.getReservationNo(), reservation.getOrderNo(), reservation.getReservationStatus(),
                reservation.getWarehouseNoValue(), reservation.getFailureReason(), reservation.getReleaseReason(),
                reservation.getCreatedAt(), reservation.getReleasedAt(), reservation.getDeductedAt());
    }

    private InventoryReservationItemDO toDataObject(InventoryReservationItem item, Long tenantId, String reservationNo) {
        return new InventoryReservationItemDO(item.getId(), tenantId, reservationNo, item.getSkuId(), item.getQuantity());
    }

    private InventoryLedger toDomain(InventoryLedgerDO dataObject) {
        return new InventoryLedger(dataObject.getId(), TenantId.of(dataObject.getTenantId()), dataObject.getOrderNo(),
                dataObject.getReservationNo(), dataObject.getSkuId(),
                dataObject.getWarehouseNo(),
                dataObject.getLedgerType(), dataObject.getQuantity(), dataObject.getOccurredAt());
    }

    private InventoryLedgerDO toDataObject(InventoryLedger ledger) {
        return new InventoryLedgerDO(ledger.getId(), ledger.getTenantIdValue(), ledger.getOrderNo(),
                ledger.getReservationNo(), ledger.getSkuId(), ledger.getWarehouseNoValue(), ledger.getLedgerType(),
                ledger.getQuantity(), ledger.getOccurredAt());
    }

    private InventoryAuditLog toDomain(InventoryAuditLogDO dataObject) {
        return new InventoryAuditLog(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getReservationNo(), dataObject.getActionType(), dataObject.getOperatorType(),
                dataObject.getOperatorId(), dataObject.getOccurredAt());
    }

    private InventoryAuditLogDO toDataObject(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDO(auditLog.getId(), auditLog.getTenantId(), auditLog.getOrderNo(),
                auditLog.getReservationNo(), auditLog.getActionType(), auditLog.getOperatorType(),
                auditLog.getOperatorId(), auditLog.getOccurredAt());
    }

    private InventoryAuditOutboxDO toDataObject(InventoryAuditOutbox outbox) {
        return new InventoryAuditOutboxDO(outbox.getIdValue(), outbox.getEventCodeValue(), outbox.getTenantId(), outbox.getOrderNo(),
                outbox.getReservationNo(), outbox.getActionType(), outbox.getOperatorType(),
                outbox.getOperatorIdValue(), outbox.getOccurredAt(), outbox.getErrorMessage(),
                outbox.getStatus().value(), outbox.getRetryCount(), outbox.getNextRetryAt(), outbox.getProcessingOwner(),
                outbox.getLeaseUntil(), outbox.getClaimedAt(), outbox.getDeadReason(), outbox.getFailedAt(),
                outbox.getUpdatedAt());
    }

    private InventoryAuditOutbox toDomain(InventoryAuditOutboxDO dataObject) {
        return new InventoryAuditOutbox(dataObject.getId(), dataObject.getEventCode(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getReservationNo(), dataObject.getActionType(), dataObject.getOperatorType(),
                dataObject.getOperatorId(), dataObject.getOccurredAt(), dataObject.getErrorMessage(),
                com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus.fromValue(dataObject.getStatus()), dataObject.getRetryCount(), dataObject.getNextRetryAt(),
                dataObject.getProcessingOwner(), dataObject.getLeaseUntil(), dataObject.getClaimedAt(),
                dataObject.getDeadReason(), dataObject.getFailedAt(), dataObject.getUpdatedAt());
    }

    private InventoryAuditDeadLetterDO toDataObject(InventoryAuditDeadLetter deadLetter) {
        return new InventoryAuditDeadLetterDO(deadLetter.getId(), deadLetter.getOutboxIdValue(), deadLetter.getEventCodeValue(), deadLetter.getTenantIdValue(),
                deadLetter.getOrderNoValue(), deadLetter.getReservationNoValue(), deadLetter.getActionTypeValue(),
                deadLetter.getOperatorTypeValue(), deadLetter.getOperatorIdValue(), deadLetter.getOccurredAt(),
                deadLetter.getRetryCount(), deadLetter.getErrorMessage(), deadLetter.getDeadReason(),
                deadLetter.getDeadAt(), deadLetter.getReplayStatusValue(), deadLetter.getReplayCount(),
                deadLetter.getLastReplayAt(), deadLetter.getLastReplayResult(), deadLetter.getLastReplayError(),
                deadLetter.getReplayKey(), deadLetter.getReplayOperatorType(), deadLetter.getReplayOperatorIdValue());
    }

    private InventoryAuditDeadLetter toDomain(InventoryAuditDeadLetterDO dataObject) {
        return new InventoryAuditDeadLetter(dataObject.getId(), dataObject.getOutboxId(), dataObject.getEventCode(),
                dataObject.getTenantId(), dataObject.getOrderNo(), dataObject.getReservationNo(),
                dataObject.getActionType(), dataObject.getOperatorType(), dataObject.getOperatorId(),
                dataObject.getOccurredAt(), dataObject.getRetryCount(), dataObject.getErrorMessage(), dataObject.getDeadReason(),
                dataObject.getDeadAt(), dataObject.getReplayStatus(), dataObject.getReplayCount(),
                dataObject.getLastReplayAt(), dataObject.getLastReplayResult(), dataObject.getLastReplayError(),
                dataObject.getReplayKey(), dataObject.getReplayOperatorType(),
                dataObject.getReplayOperatorId() == null ? null : String.valueOf(dataObject.getReplayOperatorId()));
    }

    private InventoryAuditReplayTaskDO toDataObject(InventoryAuditReplayTask task) {
        return new InventoryAuditReplayTaskDO(task.getId(), task.getTenantId(), task.getTaskNo(), task.getStatus().value(),
                task.getTotalCount(), task.getProcessedCount(), task.getSuccessCount(), task.getFailedCount(),
                task.getReplayKeyPrefix(), task.getOperatorType(), task.getOperatorIdValue(), task.getProcessingOwner(),
                task.getLeaseUntil(), task.getLastError(), task.getCreatedAt(), task.getStartedAt(), task.getPausedAt(),
                task.getFinishedAt(), task.getUpdatedAt());
    }

    private InventoryAuditReplayTask toDomain(InventoryAuditReplayTaskDO dataObject) {
        return new InventoryAuditReplayTask(dataObject.getId(), dataObject.getTenantId(), dataObject.getTaskNo(),
                com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus.fromValue(dataObject.getStatus()), dataObject.getTotalCount(), dataObject.getProcessedCount(),
                dataObject.getSuccessCount(), dataObject.getFailedCount(), dataObject.getReplayKeyPrefix(),
                dataObject.getOperatorType(), toStringValue(dataObject.getOperatorId()), dataObject.getProcessingOwner(),
                dataObject.getLeaseUntil(), dataObject.getLastError(), dataObject.getCreatedAt(),
                dataObject.getStartedAt(), dataObject.getPausedAt(), dataObject.getFinishedAt(),
                dataObject.getUpdatedAt());
    }

    private InventoryAuditReplayTaskItem toDomain(InventoryAuditReplayTaskItemDO dataObject) {
        return new InventoryAuditReplayTaskItem(dataObject.getId(), dataObject.getTaskId(),
                dataObject.getTenantId(),
                dataObject.getDeadLetterId(), dataObject.getItemStatus(), dataObject.getReplayStatus(),
                dataObject.getReplayKey(), dataObject.getResultMessage(), dataObject.getStartedAt(),
                dataObject.getFinishedAt(), dataObject.getUpdatedAt());
    }

    private String toStringValue(Long value) {
        return value == null ? null : String.valueOf(value);
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

    private Long toLongValue(String value) {
        return value == null ? null : Long.valueOf(value);
    }

}
