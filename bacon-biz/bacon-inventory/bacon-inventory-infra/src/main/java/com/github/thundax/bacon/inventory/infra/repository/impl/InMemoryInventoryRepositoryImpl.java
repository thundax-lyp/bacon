package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@ConditionalOnProperty(name = "bacon.inventory.repository.mode", havingValue = "memory")
public class InMemoryInventoryRepositoryImpl implements InventoryStockRepository, InventoryReservationRepository,
        InventoryLogRepository {

    private final AtomicLong inventoryIdGenerator = new AtomicLong(1000L);
    private final AtomicLong reservationIdGenerator = new AtomicLong(1000L);
    private final AtomicLong itemIdGenerator = new AtomicLong(1000L);
    private final AtomicLong ledgerIdGenerator = new AtomicLong(1000L);
    private final AtomicLong auditLogIdGenerator = new AtomicLong(1000L);
    private final AtomicLong auditOutboxIdGenerator = new AtomicLong(1000L);
    private final AtomicLong auditDeadLetterIdGenerator = new AtomicLong(1000L);
    private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();
    private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryLedger>> ledgers = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryAuditLog>> auditLogs = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryAuditOutbox>> auditOutbox = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryAuditDeadLetter>> auditDeadLetters = new ConcurrentHashMap<>();

    public InMemoryInventoryRepositoryImpl() {
        log.info("Using in-memory inventory repository");
    }

    @Override
    public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
        return Optional.ofNullable(inventories.get(key(tenantId, skuId)));
    }

    @Override
    public List<Inventory> findInventories(Long tenantId) {
        return inventories.values().stream()
                .filter(inventory -> inventory.getTenantId().equals(tenantId))
                .sorted(java.util.Comparator.comparing(Inventory::getSkuId))
                .toList();
    }

    @Override
    public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
        return skuIds.stream()
                .map(skuId -> inventories.get(key(tenantId, skuId)))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public List<Inventory> pageInventories(Long tenantId, Long skuId, String status, int pageNo, int pageSize) {
        return findInventories(tenantId).stream()
                .filter(inventory -> skuId == null || inventory.getSkuId().equals(skuId))
                .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    @Override
    public long countInventories(Long tenantId, Long skuId, String status) {
        return findInventories(tenantId).stream()
                .filter(inventory -> skuId == null || inventory.getSkuId().equals(skuId))
                .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                .count();
    }

    @Override
    public Inventory saveInventory(Inventory inventory) {
        if (inventory.getId() == null) {
            inventory = new Inventory(inventoryIdGenerator.getAndIncrement(), inventory.getTenantId(), inventory.getSkuId(),
                    inventory.getWarehouseId(), inventory.getOnHandQuantity(), inventory.getReservedQuantity(),
                    inventory.getAvailableQuantity(), inventory.getStatus(), inventory.getVersion(), inventory.getUpdatedAt());
        }
        Long version = inventory.getVersion() == null ? 0L : inventory.getVersion() + 1L;
        inventory.markPersisted(version);
        inventories.put(key(inventory.getTenantId(), inventory.getSkuId()), inventory);
        return inventory;
    }

    @Override
    public InventoryReservation saveReservation(InventoryReservation reservation) {
        if (reservation.getId() == null) {
            reservation = InventoryReservation.rehydrate(reservationIdGenerator.getAndIncrement(), reservation.getTenantId(),
                    reservation.getReservationNo(), reservation.getOrderNo(), reservation.getWarehouseId(),
                    reservation.getCreatedAt(), reservation.getItems().stream()
                            .map(item -> new InventoryReservationItem(
                                    item.getId() == null ? itemIdGenerator.getAndIncrement() : item.getId(),
                                    item.getTenantId(), item.getReservationNo(), item.getSkuId(), item.getQuantity()))
                            .toList(),
                    reservation.getReservationStatus(), reservation.getFailureReason(), reservation.getReleaseReason(),
                    reservation.getReleasedAt(), reservation.getDeductedAt());
        }
        reservations.put(reservationKey(reservation.getTenantId(), reservation.getOrderNo()), reservation);
        return reservation;
    }

    @Override
    public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
        return Optional.ofNullable(reservations.get(reservationKey(tenantId, orderNo)));
    }

    @Override
    public void saveLedger(InventoryLedger ledger) {
        if (ledger.getId() == null) {
            ledger = new InventoryLedger(ledgerIdGenerator.getAndIncrement(), ledger.getTenantId(), ledger.getOrderNo(),
                    ledger.getReservationNo(), ledger.getSkuId(), ledger.getWarehouseId(), ledger.getLedgerType(),
                    ledger.getQuantity(), ledger.getOccurredAt());
        }
        ledgers.computeIfAbsent(reservationKey(ledger.getTenantId(), ledger.getOrderNo()), key -> new ArrayList<>())
                .add(ledger);
    }

    @Override
    public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
        return List.copyOf(ledgers.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
    }

    @Override
    public void saveAuditLog(InventoryAuditLog auditLog) {
        if (auditLog.getId() == null) {
            auditLog = new InventoryAuditLog(auditLogIdGenerator.getAndIncrement(), auditLog.getTenantId(),
                    auditLog.getOrderNo(), auditLog.getReservationNo(), auditLog.getActionType(),
                    auditLog.getOperatorType(), auditLog.getOperatorId(), auditLog.getOccurredAt());
        }
        auditLogs.computeIfAbsent(reservationKey(auditLog.getTenantId(), auditLog.getOrderNo()), key -> new ArrayList<>())
                .add(auditLog);
    }

    @Override
    public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return List.copyOf(auditLogs.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
    }

    @Override
    public void saveAuditOutbox(InventoryAuditOutbox outbox) {
        if (outbox.getId() == null) {
            outbox = new InventoryAuditOutbox(auditOutboxIdGenerator.getAndIncrement(), outbox.getTenantId(),
                    outbox.getOrderNo(), outbox.getReservationNo(), outbox.getActionType(), outbox.getOperatorType(),
                    outbox.getOperatorId(), outbox.getOccurredAt(), outbox.getErrorMessage(), outbox.getStatus(),
                    outbox.getRetryCount(), outbox.getNextRetryAt(), outbox.getProcessingOwner(), outbox.getLeaseUntil(),
                    outbox.getClaimedAt(), outbox.getDeadReason(), outbox.getFailedAt(), outbox.getUpdatedAt());
        }
        auditOutbox.computeIfAbsent(reservationKey(outbox.getTenantId(), outbox.getOrderNo()), key -> new ArrayList<>())
                .add(outbox);
    }

    @Override
    public List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
        return auditOutbox.values().stream()
                .flatMap(List::stream)
                .filter(item -> InventoryAuditOutbox.STATUS_NEW.equals(item.getStatus())
                        || InventoryAuditOutbox.STATUS_RETRYING.equals(item.getStatus()))
                .filter(item -> item.getNextRetryAt() == null || !item.getNextRetryAt().isAfter(now))
                .sorted(java.util.Comparator.comparing(InventoryAuditOutbox::getFailedAt).thenComparing(InventoryAuditOutbox::getId))
                .limit(limit)
                .toList();
    }

    @Override
    public List<InventoryAuditOutbox> claimRetryableAuditOutbox(Instant now, int limit,
                                                                 String processingOwner, Instant leaseUntil) {
        List<InventoryAuditOutbox> claimed = new ArrayList<>(Math.max(limit, 0));
        List<InventoryAuditOutbox> candidates = findRetryableAuditOutbox(now, Math.max(limit * 3, limit));
        for (InventoryAuditOutbox candidate : candidates) {
            if (claimed.size() >= limit) {
                break;
            }
            if (!tryClaim(candidate.getId(), now, processingOwner, leaseUntil)) {
                continue;
            }
            findAuditOutboxById(candidate.getId()).ifPresent(claimed::add);
        }
        return List.copyOf(claimed);
    }

    @Override
    public int releaseExpiredAuditOutboxLease(Instant now) {
        int released = 0;
        for (List<InventoryAuditOutbox> list : auditOutbox.values()) {
            for (InventoryAuditOutbox item : list) {
                if (!InventoryAuditOutbox.STATUS_PROCESSING.equals(item.getStatus())) {
                    continue;
                }
                if (item.getLeaseUntil() == null || item.getLeaseUntil().isAfter(now)) {
                    continue;
                }
                item.setStatus(InventoryAuditOutbox.STATUS_RETRYING);
                item.setProcessingOwner(null);
                item.setLeaseUntil(null);
                item.setClaimedAt(null);
                item.setUpdatedAt(now);
                released++;
            }
        }
        return released;
    }

    @Override
    public void updateAuditOutboxForRetry(Long outboxId, int retryCount, Instant nextRetryAt, String errorMessage,
                                          Instant updatedAt) {
        findAuditOutboxById(outboxId).ifPresent(item -> {
            item.setStatus(InventoryAuditOutbox.STATUS_RETRYING);
            item.setRetryCount(retryCount);
            item.setNextRetryAt(nextRetryAt);
            item.setErrorMessage(errorMessage);
            item.setUpdatedAt(updatedAt);
        });
    }

    @Override
    public boolean updateAuditOutboxForRetryClaimed(Long outboxId, String processingOwner, int retryCount,
                                                    Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        return findAuditOutboxById(outboxId)
                .filter(item -> InventoryAuditOutbox.STATUS_PROCESSING.equals(item.getStatus()))
                .filter(item -> processingOwner.equals(item.getProcessingOwner()))
                .map(item -> {
                    item.setStatus(InventoryAuditOutbox.STATUS_RETRYING);
                    item.setRetryCount(retryCount);
                    item.setNextRetryAt(nextRetryAt);
                    item.setErrorMessage(errorMessage);
                    item.setProcessingOwner(null);
                    item.setLeaseUntil(null);
                    item.setClaimedAt(null);
                    item.setUpdatedAt(updatedAt);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public void markAuditOutboxDead(Long outboxId, int retryCount, String deadReason, Instant updatedAt) {
        findAuditOutboxById(outboxId).ifPresent(item -> {
            item.setStatus(InventoryAuditOutbox.STATUS_DEAD);
            item.setRetryCount(retryCount);
            item.setDeadReason(deadReason);
            item.setUpdatedAt(updatedAt);
        });
    }

    @Override
    public boolean markAuditOutboxDeadClaimed(Long outboxId, String processingOwner, int retryCount,
                                              String deadReason, Instant updatedAt) {
        return findAuditOutboxById(outboxId)
                .filter(item -> InventoryAuditOutbox.STATUS_PROCESSING.equals(item.getStatus()))
                .filter(item -> processingOwner.equals(item.getProcessingOwner()))
                .map(item -> {
                    item.setStatus(InventoryAuditOutbox.STATUS_DEAD);
                    item.setRetryCount(retryCount);
                    item.setDeadReason(deadReason);
                    item.setProcessingOwner(null);
                    item.setLeaseUntil(null);
                    item.setClaimedAt(null);
                    item.setUpdatedAt(updatedAt);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public void deleteAuditOutbox(Long outboxId) {
        auditOutbox.values().forEach(list -> list.removeIf(item -> item.getId().equals(outboxId)));
    }

    @Override
    public boolean deleteAuditOutboxClaimed(Long outboxId, String processingOwner) {
        for (List<InventoryAuditOutbox> list : auditOutbox.values()) {
            java.util.Iterator<InventoryAuditOutbox> iterator = list.iterator();
            while (iterator.hasNext()) {
                InventoryAuditOutbox item = iterator.next();
                if (!item.getId().equals(outboxId)) {
                    continue;
                }
                if (!InventoryAuditOutbox.STATUS_PROCESSING.equals(item.getStatus())
                        || !processingOwner.equals(item.getProcessingOwner())) {
                    return false;
                }
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
        if (deadLetter.getId() == null) {
            deadLetter = new InventoryAuditDeadLetter(auditDeadLetterIdGenerator.getAndIncrement(), deadLetter.getOutboxId(),
                    deadLetter.getTenantId(), deadLetter.getOrderNo(), deadLetter.getReservationNo(),
                    deadLetter.getActionType(), deadLetter.getOperatorType(), deadLetter.getOperatorId(),
                    deadLetter.getOccurredAt(), deadLetter.getRetryCount(), deadLetter.getErrorMessage(),
                    deadLetter.getDeadReason(), deadLetter.getDeadAt(), deadLetter.getReplayStatus(),
                    deadLetter.getReplayCount(), deadLetter.getLastReplayAt(), deadLetter.getLastReplayResult(),
                    deadLetter.getLastReplayError(), deadLetter.getReplayKey(), deadLetter.getReplayOperatorType(),
                    deadLetter.getReplayOperatorId());
        }
        auditDeadLetters.computeIfAbsent(reservationKey(deadLetter.getTenantId(), deadLetter.getOrderNo()),
                        key -> new ArrayList<>())
                .add(deadLetter);
    }

    @Override
    public List<InventoryAuditDeadLetter> pageAuditDeadLetters(Long tenantId, String orderNo,
                                                                String replayStatus, int pageNo, int pageSize) {
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getTenantId().equals(tenantId))
                .filter(item -> orderNo == null || orderNo.isBlank() || orderNo.equals(item.getOrderNo()))
                .filter(item -> replayStatus == null || replayStatus.isBlank()
                        || replayStatus.equals(item.getReplayStatus()))
                .sorted(java.util.Comparator.comparing(InventoryAuditDeadLetter::getDeadAt).reversed()
                        .thenComparing(InventoryAuditDeadLetter::getId, java.util.Comparator.reverseOrder()))
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    @Override
    public long countAuditDeadLetters(Long tenantId, String orderNo, String replayStatus) {
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getTenantId().equals(tenantId))
                .filter(item -> orderNo == null || orderNo.isBlank() || orderNo.equals(item.getOrderNo()))
                .filter(item -> replayStatus == null || replayStatus.isBlank()
                        || replayStatus.equals(item.getReplayStatus()))
                .count();
    }

    @Override
    public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(Long id) {
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean claimAuditDeadLetterForReplay(Long id, Long tenantId, String replayKey,
                                                 String operatorType, Long operatorId, Instant replayAt) {
        return findAuditDeadLetterById(id)
                .filter(item -> item.getTenantId().equals(tenantId))
                .filter(item -> InventoryAuditDeadLetter.REPLAY_STATUS_PENDING.equals(item.getReplayStatus())
                        || InventoryAuditDeadLetter.REPLAY_STATUS_FAILED.equals(item.getReplayStatus()))
                .map(item -> {
                    item.setReplayStatus(InventoryAuditDeadLetter.REPLAY_STATUS_RUNNING);
                    item.setReplayKey(replayKey);
                    item.setReplayOperatorType(operatorType);
                    item.setReplayOperatorId(operatorId);
                    item.setLastReplayAt(replayAt);
                    item.setLastReplayResult("RUNNING");
                    item.setLastReplayError(null);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public void markAuditDeadLetterReplaySuccess(Long id, String replayKey, String operatorType, Long operatorId,
                                                 Instant replayAt) {
        findAuditDeadLetterById(id).ifPresent(item -> {
            item.setReplayStatus(InventoryAuditDeadLetter.REPLAY_STATUS_SUCCEEDED);
            item.setReplayCount((item.getReplayCount() == null ? 0 : item.getReplayCount()) + 1);
            item.setReplayKey(replayKey);
            item.setReplayOperatorType(operatorType);
            item.setReplayOperatorId(operatorId);
            item.setLastReplayAt(replayAt);
            item.setLastReplayResult("SUCCEEDED");
            item.setLastReplayError(null);
        });
    }

    @Override
    public void markAuditDeadLetterReplayFailed(Long id, String replayKey, String operatorType, Long operatorId,
                                                String replayError, Instant replayAt) {
        findAuditDeadLetterById(id).ifPresent(item -> {
            item.setReplayStatus(InventoryAuditDeadLetter.REPLAY_STATUS_FAILED);
            item.setReplayCount((item.getReplayCount() == null ? 0 : item.getReplayCount()) + 1);
            item.setReplayKey(replayKey);
            item.setReplayOperatorType(operatorType);
            item.setReplayOperatorId(operatorId);
            item.setLastReplayAt(replayAt);
            item.setLastReplayResult("FAILED");
            item.setLastReplayError(replayError);
        });
    }

    private static String key(Long tenantId, Long skuId) {
        return tenantId + ":" + skuId;
    }

    private static String reservationKey(Long tenantId, String orderNo) {
        return tenantId + ":" + orderNo;
    }

    private Optional<InventoryAuditOutbox> findAuditOutboxById(Long outboxId) {
        return auditOutbox.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getId().equals(outboxId))
                .findFirst();
    }

    private boolean tryClaim(Long outboxId, Instant now, String processingOwner, Instant leaseUntil) {
        return findAuditOutboxById(outboxId)
                .filter(item -> InventoryAuditOutbox.STATUS_NEW.equals(item.getStatus())
                        || InventoryAuditOutbox.STATUS_RETRYING.equals(item.getStatus()))
                .filter(item -> item.getNextRetryAt() == null || !item.getNextRetryAt().isAfter(now))
                .map(item -> {
                    item.setStatus(InventoryAuditOutbox.STATUS_PROCESSING);
                    item.setProcessingOwner(processingOwner);
                    item.setLeaseUntil(leaseUntil);
                    item.setClaimedAt(now);
                    item.setUpdatedAt(now);
                    return true;
                })
                .orElse(false);
    }
}
