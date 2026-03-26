package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditLogDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditDeadLetterDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditOutboxDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryLedgerDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationDO;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationItemDO;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditLogMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditDeadLetterMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryAuditOutboxMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryLedgerMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryReservationItemMapper;
import com.github.thundax.bacon.inventory.infra.persistence.mapper.InventoryReservationMapper;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.Instant;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@ConditionalOnProperty(name = "bacon.inventory.repository.mode", havingValue = "strict", matchIfMissing = true)
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
public class InventoryRepositoryImpl implements InventoryStockRepository, InventoryReservationRepository, InventoryLogRepository {

    private final InventoryMapper inventoryMapper;
    private final InventoryReservationMapper reservationMapper;
    private final InventoryReservationItemMapper reservationItemMapper;
    private final InventoryLedgerMapper ledgerMapper;
    private final InventoryAuditLogMapper auditLogMapper;
    private final InventoryAuditOutboxMapper auditOutboxMapper;
    private final InventoryAuditDeadLetterMapper auditDeadLetterMapper;

    public InventoryRepositoryImpl(InventoryMapper inventoryMapper,
                                   InventoryReservationMapper reservationMapper,
                                   InventoryReservationItemMapper reservationItemMapper,
                                   InventoryLedgerMapper ledgerMapper,
                                   InventoryAuditLogMapper auditLogMapper,
                                   InventoryAuditOutboxMapper auditOutboxMapper,
                                   InventoryAuditDeadLetterMapper auditDeadLetterMapper) {
        this.inventoryMapper = inventoryMapper;
        this.reservationMapper = reservationMapper;
        this.reservationItemMapper = reservationItemMapper;
        this.ledgerMapper = ledgerMapper;
        this.auditLogMapper = auditLogMapper;
        this.auditOutboxMapper = auditOutboxMapper;
        this.auditDeadLetterMapper = auditDeadLetterMapper;
        log.info("Using MyBatis-Plus inventory repository");
    }

    @Override
    public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
        return Optional.ofNullable(inventoryMapper.selectOne(Wrappers.<InventoryDO>lambdaQuery()
                .eq(InventoryDO::getTenantId, tenantId)
                .eq(InventoryDO::getSkuId, skuId)))
                .map(this::toDomain);
    }

    @Override
    public List<Inventory> findInventories(Long tenantId) {
        return inventoryMapper.selectList(Wrappers.<InventoryDO>lambdaQuery()
                        .eq(InventoryDO::getTenantId, tenantId)
                        .orderByAsc(InventoryDO::getSkuId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
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

    @Override
    public List<Inventory> pageInventories(Long tenantId, Long skuId, String status, int pageNo, int pageSize) {
        long offset = (long) (pageNo - 1) * pageSize;
        return inventoryMapper.selectPageByCondition(tenantId, skuId, status, offset, pageSize)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countInventories(Long tenantId, Long skuId, String status) {
        return inventoryMapper.countByCondition(tenantId, skuId, status);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void saveLedger(InventoryLedger ledger) {
        ledgerMapper.insert(toDataObject(ledger));
    }

    @Override
    public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
        return ledgerMapper.selectList(Wrappers.<InventoryLedgerDO>lambdaQuery()
                        .eq(InventoryLedgerDO::getTenantId, tenantId)
                        .eq(InventoryLedgerDO::getOrderNo, orderNo)
                        .orderByAsc(InventoryLedgerDO::getOccurredAt, InventoryLedgerDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveAuditLog(InventoryAuditLog auditLog) {
        auditLogMapper.insert(toDataObject(auditLog));
    }

    @Override
    public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return auditLogMapper.selectList(Wrappers.<InventoryAuditLogDO>lambdaQuery()
                        .eq(InventoryAuditLogDO::getTenantId, tenantId)
                        .eq(InventoryAuditLogDO::getOrderNo, orderNo)
                        .orderByAsc(InventoryAuditLogDO::getOccurredAt, InventoryAuditLogDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveAuditOutbox(InventoryAuditOutbox outbox) {
        auditOutboxMapper.insert(toDataObject(outbox));
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void updateAuditOutboxForRetry(Long outboxId, int retryCount, Instant nextRetryAt, String errorMessage,
                                          Instant updatedAt) {
        auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                .eq(InventoryAuditOutboxDO::getId, outboxId)
                .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_RETRYING)
                .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                .set(InventoryAuditOutboxDO::getNextRetryAt, nextRetryAt)
                .set(InventoryAuditOutboxDO::getErrorMessage, errorMessage)
                .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt));
    }

    @Override
    public boolean updateAuditOutboxForRetryClaimed(Long outboxId, String processingOwner, int retryCount,
                                                    Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        return auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                .eq(InventoryAuditOutboxDO::getId, outboxId)
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

    @Override
    public void markAuditOutboxDead(Long outboxId, int retryCount, String deadReason, Instant updatedAt) {
        auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                .eq(InventoryAuditOutboxDO::getId, outboxId)
                .set(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_DEAD)
                .set(InventoryAuditOutboxDO::getRetryCount, retryCount)
                .set(InventoryAuditOutboxDO::getDeadReason, deadReason)
                .set(InventoryAuditOutboxDO::getUpdatedAt, updatedAt));
    }

    @Override
    public boolean markAuditOutboxDeadClaimed(Long outboxId, String processingOwner, int retryCount,
                                              String deadReason, Instant updatedAt) {
        return auditOutboxMapper.update(null, Wrappers.<InventoryAuditOutboxDO>lambdaUpdate()
                .eq(InventoryAuditOutboxDO::getId, outboxId)
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

    @Override
    public void deleteAuditOutbox(Long outboxId) {
        auditOutboxMapper.deleteById(outboxId);
    }

    @Override
    public boolean deleteAuditOutboxClaimed(Long outboxId, String processingOwner) {
        return auditOutboxMapper.delete(Wrappers.<InventoryAuditOutboxDO>lambdaQuery()
                .eq(InventoryAuditOutboxDO::getId, outboxId)
                .eq(InventoryAuditOutboxDO::getStatus, InventoryAuditOutbox.STATUS_PROCESSING)
                .eq(InventoryAuditOutboxDO::getProcessingOwner, processingOwner)) > 0;
    }

    @Override
    public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
        auditDeadLetterMapper.insert(toDataObject(deadLetter));
    }

    @Override
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
                        .orderByDesc(InventoryAuditDeadLetterDO::getDeadAt, InventoryAuditDeadLetterDO::getId)
                        .last("limit " + offset + ", " + pageSize))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
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

    @Override
    public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(Long id) {
        return Optional.ofNullable(auditDeadLetterMapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public boolean claimAuditDeadLetterForReplay(Long id, Long tenantId, String replayKey,
                                                 String operatorType, Long operatorId, Instant replayAt) {
        return auditDeadLetterMapper.update(null, Wrappers.<InventoryAuditDeadLetterDO>lambdaUpdate()
                .eq(InventoryAuditDeadLetterDO::getId, id)
                .eq(InventoryAuditDeadLetterDO::getTenantId, tenantId)
                .in(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditDeadLetter.REPLAY_STATUS_PENDING,
                        InventoryAuditDeadLetter.REPLAY_STATUS_FAILED)
                .set(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditDeadLetter.REPLAY_STATUS_RUNNING)
                .set(InventoryAuditDeadLetterDO::getReplayKey, replayKey)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorType, operatorType)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorId, operatorId)
                .set(InventoryAuditDeadLetterDO::getLastReplayAt, replayAt)
                .set(InventoryAuditDeadLetterDO::getLastReplayResult, "RUNNING")
                .set(InventoryAuditDeadLetterDO::getLastReplayError, null)) > 0;
    }

    @Override
    public void markAuditDeadLetterReplaySuccess(Long id, String replayKey, String operatorType, Long operatorId,
                                                 Instant replayAt) {
        auditDeadLetterMapper.update(null, Wrappers.<InventoryAuditDeadLetterDO>lambdaUpdate()
                .eq(InventoryAuditDeadLetterDO::getId, id)
                .set(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditDeadLetter.REPLAY_STATUS_SUCCEEDED)
                .setSql("replay_count = ifnull(replay_count, 0) + 1")
                .set(InventoryAuditDeadLetterDO::getReplayKey, replayKey)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorType, operatorType)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorId, operatorId)
                .set(InventoryAuditDeadLetterDO::getLastReplayAt, replayAt)
                .set(InventoryAuditDeadLetterDO::getLastReplayResult, "SUCCEEDED")
                .set(InventoryAuditDeadLetterDO::getLastReplayError, null));
    }

    @Override
    public void markAuditDeadLetterReplayFailed(Long id, String replayKey, String operatorType, Long operatorId,
                                                String replayError, Instant replayAt) {
        auditDeadLetterMapper.update(null, Wrappers.<InventoryAuditDeadLetterDO>lambdaUpdate()
                .eq(InventoryAuditDeadLetterDO::getId, id)
                .set(InventoryAuditDeadLetterDO::getReplayStatus, InventoryAuditDeadLetter.REPLAY_STATUS_FAILED)
                .setSql("replay_count = ifnull(replay_count, 0) + 1")
                .set(InventoryAuditDeadLetterDO::getReplayKey, replayKey)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorType, operatorType)
                .set(InventoryAuditDeadLetterDO::getReplayOperatorId, operatorId)
                .set(InventoryAuditDeadLetterDO::getLastReplayAt, replayAt)
                .set(InventoryAuditDeadLetterDO::getLastReplayResult, "FAILED")
                .set(InventoryAuditDeadLetterDO::getLastReplayError, replayError));
    }

    private Inventory toDomain(InventoryDO dataObject) {
        return new Inventory(dataObject.getId(), dataObject.getTenantId(), dataObject.getSkuId(), dataObject.getWarehouseId(),
                dataObject.getOnHandQuantity(), dataObject.getReservedQuantity(), dataObject.getAvailableQuantity(),
                dataObject.getStatus(), dataObject.getVersion(),
                dataObject.getUpdatedAt() == null ? dataObject.getCreatedAt() : dataObject.getUpdatedAt());
    }

    private InventoryDO toDataObject(Inventory inventory) {
        return new InventoryDO(inventory.getId(), inventory.getTenantId(), inventory.getSkuId(),
                inventory.getWarehouseId(), inventory.getOnHandQuantity(), inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(), inventory.getStatus(), inventory.getVersion(), null,
                inventory.getUpdatedAt(), null,
                inventory.getUpdatedAt());
    }

    private InventoryReservation toDomain(InventoryReservationDO reservation, List<InventoryReservationItem> items) {
        return InventoryReservation.rehydrate(reservation.getId(), reservation.getTenantId(),
                reservation.getReservationNo(), reservation.getOrderNo(), reservation.getWarehouseId(),
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
                reservation.getWarehouseId(), reservation.getFailureReason(), reservation.getReleaseReason(),
                reservation.getCreatedAt(), reservation.getReleasedAt(), reservation.getDeductedAt());
    }

    private InventoryReservationItemDO toDataObject(InventoryReservationItem item, Long tenantId, String reservationNo) {
        return new InventoryReservationItemDO(item.getId(), tenantId, reservationNo, item.getSkuId(), item.getQuantity());
    }

    private InventoryLedger toDomain(InventoryLedgerDO dataObject) {
        return new InventoryLedger(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getReservationNo(), dataObject.getSkuId(), dataObject.getWarehouseId(),
                dataObject.getLedgerType(), dataObject.getQuantity(), dataObject.getOccurredAt());
    }

    private InventoryLedgerDO toDataObject(InventoryLedger ledger) {
        return new InventoryLedgerDO(ledger.getId(), ledger.getTenantId(), ledger.getOrderNo(),
                ledger.getReservationNo(), ledger.getSkuId(), ledger.getWarehouseId(), ledger.getLedgerType(),
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
        return new InventoryAuditOutboxDO(outbox.getId(), outbox.getTenantId(), outbox.getOrderNo(),
                outbox.getReservationNo(), outbox.getActionType(), outbox.getOperatorType(),
                outbox.getOperatorId(), outbox.getOccurredAt(), outbox.getErrorMessage(),
                outbox.getStatus(), outbox.getRetryCount(), outbox.getNextRetryAt(), outbox.getProcessingOwner(),
                outbox.getLeaseUntil(), outbox.getClaimedAt(), outbox.getDeadReason(), outbox.getFailedAt(),
                outbox.getUpdatedAt());
    }

    private InventoryAuditOutbox toDomain(InventoryAuditOutboxDO dataObject) {
        return new InventoryAuditOutbox(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getReservationNo(), dataObject.getActionType(), dataObject.getOperatorType(),
                dataObject.getOperatorId(), dataObject.getOccurredAt(), dataObject.getErrorMessage(),
                dataObject.getStatus(), dataObject.getRetryCount(), dataObject.getNextRetryAt(),
                dataObject.getProcessingOwner(), dataObject.getLeaseUntil(), dataObject.getClaimedAt(),
                dataObject.getDeadReason(), dataObject.getFailedAt(), dataObject.getUpdatedAt());
    }

    private InventoryAuditDeadLetterDO toDataObject(InventoryAuditDeadLetter deadLetter) {
        return new InventoryAuditDeadLetterDO(deadLetter.getId(), deadLetter.getOutboxId(), deadLetter.getTenantId(),
                deadLetter.getOrderNo(), deadLetter.getReservationNo(), deadLetter.getActionType(),
                deadLetter.getOperatorType(), deadLetter.getOperatorId(), deadLetter.getOccurredAt(),
                deadLetter.getRetryCount(), deadLetter.getErrorMessage(), deadLetter.getDeadReason(),
                deadLetter.getDeadAt(), deadLetter.getReplayStatus(), deadLetter.getReplayCount(),
                deadLetter.getLastReplayAt(), deadLetter.getLastReplayResult(), deadLetter.getLastReplayError(),
                deadLetter.getReplayKey(), deadLetter.getReplayOperatorType(), deadLetter.getReplayOperatorId());
    }

    private InventoryAuditDeadLetter toDomain(InventoryAuditDeadLetterDO dataObject) {
        return new InventoryAuditDeadLetter(dataObject.getId(), dataObject.getOutboxId(), dataObject.getTenantId(),
                dataObject.getOrderNo(), dataObject.getReservationNo(), dataObject.getActionType(),
                dataObject.getOperatorType(), dataObject.getOperatorId(), dataObject.getOccurredAt(),
                dataObject.getRetryCount(), dataObject.getErrorMessage(), dataObject.getDeadReason(),
                dataObject.getDeadAt(), dataObject.getReplayStatus(), dataObject.getReplayCount(),
                dataObject.getLastReplayAt(), dataObject.getLastReplayResult(), dataObject.getLastReplayError(),
                dataObject.getReplayKey(), dataObject.getReplayOperatorType(), dataObject.getReplayOperatorId());
    }
}
