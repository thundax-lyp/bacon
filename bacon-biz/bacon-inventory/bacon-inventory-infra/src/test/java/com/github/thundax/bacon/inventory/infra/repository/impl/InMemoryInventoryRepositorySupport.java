package com.github.thundax.bacon.inventory.infra.repository.impl;

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
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Profile("test")
public class InMemoryInventoryRepositorySupport {

    private static final DateTimeFormatter EVENT_CODE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AtomicLong inventoryIdGenerator = new AtomicLong(1000L);
    private final AtomicLong reservationIdGenerator = new AtomicLong(1000L);
    private final AtomicLong itemIdGenerator = new AtomicLong(1000L);
    private final AtomicLong ledgerIdGenerator = new AtomicLong(1000L);
    private final AtomicLong auditLogIdGenerator = new AtomicLong(1000L);
    private final AtomicLong auditOutboxIdGenerator = new AtomicLong(1000L);
    private final AtomicLong auditOutboxEventCodeGenerator = new AtomicLong(1000L);
    private final AtomicLong auditReplayTaskIdGenerator = new AtomicLong(1000L);
    private final AtomicLong auditReplayTaskItemIdGenerator = new AtomicLong(1000L);
    private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();
    private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryLedger>> ledgers = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryAuditLog>> auditLogs = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryAuditOutbox>> auditOutbox = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryAuditDeadLetter>> auditDeadLetters = new ConcurrentHashMap<>();
    private final Map<Long, InventoryAuditReplayTask> auditReplayTasks = new ConcurrentHashMap<>();
    private final Map<Long, List<InventoryAuditReplayTaskItem>> auditReplayTaskItems = new ConcurrentHashMap<>();

    public InMemoryInventoryRepositorySupport() {
        log.info("Using in-memory inventory repository");
    }

    public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
        return Optional.ofNullable(inventories.get(key(tenantId, skuId)));
    }

    public List<Inventory> findInventories(Long tenantId) {
        return inventories.values().stream()
                .filter(inventory -> inventory.getTenantId().value().equals(String.valueOf(tenantId)))
                .sorted(java.util.Comparator.comparing(inventory -> inventory.getSkuId().value()))
                .toList();
    }

    public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
        return skuIds.stream()
                .map(skuId -> inventories.get(key(tenantId, skuId)))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public List<Inventory> pageInventories(Long tenantId, Long skuId, String status, int pageNo, int pageSize) {
        return findInventories(tenantId).stream()
                .filter(inventory -> skuId == null || inventory.getSkuId().value().equals(skuId))
                .filter(inventory -> status == null || status.equals(inventory.getStatus().value()))
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    public long countInventories(Long tenantId, Long skuId, String status) {
        return findInventories(tenantId).stream()
                .filter(inventory -> skuId == null || inventory.getSkuId().value().equals(skuId))
                .filter(inventory -> status == null || status.equals(inventory.getStatus().value()))
                .count();
    }

    public Inventory saveInventory(Inventory inventory) {
        if (inventory.getId() == null) {
            inventory = new Inventory(inventoryIdGenerator.getAndIncrement(), inventory.getTenantId().value(),
                    inventory.getSkuId().value(), inventory.getWarehouseNo().value(), inventory.getOnHandQuantity(),
                    inventory.getReservedQuantity(), inventory.getAvailableQuantity(), inventory.getStatus(),
                    inventory.getVersion(), inventory.getUpdatedAt());
        }
        Long version = inventory.getVersion() == null ? 0L : inventory.getVersion() + 1L;
        inventory.markPersisted(version);
        inventories.put(key(inventory.getTenantId().value(), inventory.getSkuId().value()), inventory);
        return inventory;
    }

    public InventoryReservation saveReservation(InventoryReservation reservation) {
        if (reservation.getId() == null) {
            reservation = InventoryReservation.rehydrate(reservationIdGenerator.getAndIncrement(), reservation.getTenantIdValue(),
                    reservation.getReservationNoValue(), reservation.getOrderNoValue(), reservation.getWarehouseNoValue(),
                    reservation.getCreatedAt(), reservation.getItems().stream()
                            .map(item -> new InventoryReservationItem(
                                    item.getId() == null ? itemIdGenerator.getAndIncrement() : item.getId(),
                                    item.getTenantIdValue(), item.getReservationNoValue(), item.getSkuIdValue(), item.getQuantity()))
                            .toList(),
                    reservation.getReservationStatusValue(), reservation.getFailureReason(), reservation.getReleaseReason(),
                    reservation.getReleasedAt(), reservation.getDeductedAt());
        }
        reservations.put(reservationKey(reservation.getTenantIdValue(), reservation.getOrderNoValue()), reservation);
        return reservation;
    }

    public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
        return Optional.ofNullable(reservations.get(reservationKey(tenantId, orderNo)));
    }

    public void saveLedger(InventoryLedger ledger) {
        if (ledger.getId() == null) {
            ledger = new InventoryLedger(ledgerIdGenerator.getAndIncrement(), ledger.getTenantIdValue(),
                    ledger.getOrderNoValue(), ledger.getReservationNoValue(), ledger.getSkuIdValue(),
                    ledger.getWarehouseNoValue(), ledger.getLedgerType(), 
                    ledger.getQuantity(), ledger.getOccurredAt());
        }
        ledgers.computeIfAbsent(reservationKey(ledger.getTenantIdValue(), ledger.getOrderNoValue()), key -> new ArrayList<>())
                .add(ledger);
    }

    public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
        return List.copyOf(ledgers.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
    }

    public void saveAuditLog(InventoryAuditLog auditLog) {
        if (auditLog.getId() == null) {
            auditLog = new InventoryAuditLog(auditLogIdGenerator.getAndIncrement(), auditLog.getTenantId(),
                    auditLog.getOrderNo(), auditLog.getReservationNo(), auditLog.getActionType(),
                    auditLog.getOperatorType(), auditLog.getOperatorId(), auditLog.getOccurredAt());
        }
        auditLogs.computeIfAbsent(reservationKey(auditLog.getTenantIdValue(), auditLog.getOrderNoValue()),
                        key -> new ArrayList<>())
                .add(auditLog);
    }

    public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return List.copyOf(auditLogs.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
    }

    public void saveAuditOutbox(InventoryAuditOutbox outbox) {
        String eventCode = outbox.getEventCodeValue();
        if (eventCode == null) {
            eventCode = generateEventCode().value();
        }
        if (outbox.getId() == null) {
            outbox = new InventoryAuditOutbox(
                    auditOutboxIdGenerator.getAndIncrement(),
                    eventCode,
                    outbox.getTenantIdValue(),
                    outbox.getOrderNoValue(),
                    outbox.getReservationNoValue(),
                    outbox.getActionType(),
                    outbox.getOperatorType(),
                    outbox.getOperatorIdValue(),
                    outbox.getOccurredAt(),
                    outbox.getErrorMessage(),
                    outbox.getStatus(),
                    outbox.getRetryCount(),
                    outbox.getNextRetryAt(),
                    outbox.getProcessingOwner(),
                    outbox.getLeaseUntil(),
                    outbox.getClaimedAt(),
                    outbox.getDeadReason(),
                    outbox.getFailedAt(),
                    outbox.getUpdatedAt());
        } else if (outbox.getEventCode() == null) {
            outbox.setEventCode(EventCode.of(eventCode));
        }
        auditOutbox.computeIfAbsent(reservationKey(outbox.getTenantIdValue(), outbox.getOrderNoValue()),
                        key -> new ArrayList<>())
                .add(outbox);
    }

    public List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
        return auditOutbox.values().stream()
                .flatMap(List::stream)
                .filter(item -> InventoryAuditOutboxStatus.NEW.equals(item.getStatus())
                        || InventoryAuditOutboxStatus.RETRYING.equals(item.getStatus()))
                .filter(item -> item.getNextRetryAt() == null || !item.getNextRetryAt().isAfter(now))
                .sorted(java.util.Comparator.comparing(InventoryAuditOutbox::getFailedAt)
                        .thenComparing(InventoryAuditOutbox::getIdValue))
                .limit(limit)
                .toList();
    }

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

    public int releaseExpiredAuditOutboxLease(Instant now) {
        int released = 0;
        for (List<InventoryAuditOutbox> list : auditOutbox.values()) {
            for (InventoryAuditOutbox item : list) {
                if (!InventoryAuditOutboxStatus.PROCESSING.equals(item.getStatus())) {
                    continue;
                }
                if (item.getLeaseUntil() == null || item.getLeaseUntil().isAfter(now)) {
                    continue;
                }
                item.setStatus(InventoryAuditOutboxStatus.RETRYING);
                item.setProcessingOwner(null);
                item.setLeaseUntil(null);
                item.setClaimedAt(null);
                item.setUpdatedAt(now);
                released++;
            }
        }
        return released;
    }

    public void updateAuditOutboxForRetry(OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage,
                                          Instant updatedAt) {
        findAuditOutboxById(outboxId).ifPresent(item -> {
            item.setStatus(InventoryAuditOutboxStatus.RETRYING);
            item.setRetryCount(retryCount);
            item.setNextRetryAt(nextRetryAt);
            item.setErrorMessage(errorMessage);
            item.setUpdatedAt(updatedAt);
        });
    }

    public boolean updateAuditOutboxForRetryClaimed(OutboxId outboxId, String processingOwner, int retryCount,
                                                    Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        return findAuditOutboxById(outboxId)
                .filter(item -> InventoryAuditOutboxStatus.PROCESSING.equals(item.getStatus()))
                .filter(item -> processingOwner.equals(item.getProcessingOwner()))
                .map(item -> {
                    item.setStatus(InventoryAuditOutboxStatus.RETRYING);
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

    public void markAuditOutboxDead(OutboxId outboxId, int retryCount, String deadReason, Instant updatedAt) {
        findAuditOutboxById(outboxId).ifPresent(item -> {
            item.setStatus(InventoryAuditOutboxStatus.DEAD);
            item.setRetryCount(retryCount);
            item.setDeadReason(deadReason);
            item.setUpdatedAt(updatedAt);
        });
    }

    public boolean markAuditOutboxDeadClaimed(OutboxId outboxId, String processingOwner, int retryCount,
                                              String deadReason, Instant updatedAt) {
        return findAuditOutboxById(outboxId)
                .filter(item -> InventoryAuditOutboxStatus.PROCESSING.equals(item.getStatus()))
                .filter(item -> processingOwner.equals(item.getProcessingOwner()))
                .map(item -> {
                    item.setStatus(InventoryAuditOutboxStatus.DEAD);
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

    public void deleteAuditOutbox(OutboxId outboxId) {
        auditOutbox.values().forEach(list -> list.removeIf(item -> item.getId().equals(outboxId)));
    }

    public boolean deleteAuditOutboxClaimed(OutboxId outboxId, String processingOwner) {
        for (List<InventoryAuditOutbox> list : auditOutbox.values()) {
            java.util.Iterator<InventoryAuditOutbox> iterator = list.iterator();
            while (iterator.hasNext()) {
                InventoryAuditOutbox item = iterator.next();
                if (!item.getId().equals(outboxId)) {
                    continue;
                }
                if (!InventoryAuditOutboxStatus.PROCESSING.equals(item.getStatus())
                        || !processingOwner.equals(item.getProcessingOwner())) {
                    return false;
                }
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
        auditDeadLetters.computeIfAbsent(reservationKey(deadLetter.getTenantId().value(), deadLetter.getOrderNo().value()),
                        key -> new ArrayList<>())
                .add(deadLetter);
    }

    public List<InventoryAuditDeadLetter> pageAuditDeadLetters(Long tenantId, String orderNo,
                                                                String replayStatus, int pageNo, int pageSize) {
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getTenantId().value().equals(String.valueOf(tenantId)))
                .filter(item -> orderNo == null || orderNo.isBlank() || orderNo.equals(item.getOrderNo().value()))
                .filter(item -> replayStatus == null || replayStatus.isBlank()
                        || replayStatus.equals(item.getReplayStatusValue()))
                .sorted(java.util.Comparator.comparing(InventoryAuditDeadLetter::getDeadAt).reversed()
                        .thenComparing(InventoryAuditDeadLetter::getOutboxIdValue, java.util.Comparator.reverseOrder()))
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    public long countAuditDeadLetters(Long tenantId, String orderNo, String replayStatus) {
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getTenantId().value().equals(String.valueOf(tenantId)))
                .filter(item -> orderNo == null || orderNo.isBlank() || orderNo.equals(item.getOrderNo().value()))
                .filter(item -> replayStatus == null || replayStatus.isBlank()
                        || replayStatus.equals(item.getReplayStatusValue()))
                .count();
    }

    public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(Long id) {
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> java.util.Objects.equals(item.getOutboxIdValue(), id))
                .findFirst();
    }

    public boolean claimAuditDeadLetterForReplay(Long id, Long tenantId, String replayKey,
                                                 String operatorType, Long operatorId, Instant replayAt) {
        return findAuditDeadLetterById(id)
                .filter(item -> item.getTenantId().value().equals(String.valueOf(tenantId)))
                .filter(item -> InventoryAuditReplayStatus.PENDING.equals(item.getReplayStatus())
                        || InventoryAuditReplayStatus.FAILED.equals(item.getReplayStatus()))
                .map(item -> {
                    item.setReplayStatus(InventoryAuditReplayStatus.RUNNING);
                    item.setReplayKey(replayKey);
                    item.setReplayOperatorType(operatorType);
                    item.setReplayOperatorId(String.valueOf(operatorId));
                    item.setLastReplayAt(replayAt);
                    item.setLastReplayResult("RUNNING");
                    item.setLastReplayError(null);
                    return true;
                })
                .orElse(false);
    }

    public void markAuditDeadLetterReplaySuccess(Long id, String replayKey, String operatorType, Long operatorId,
                                                 Instant replayAt) {
        findAuditDeadLetterById(id).ifPresent(item -> {
            item.setReplayStatus(InventoryAuditReplayStatus.SUCCEEDED);
            item.setReplayCount((item.getReplayCount() == null ? 0 : item.getReplayCount()) + 1);
            item.setReplayKey(replayKey);
            item.setReplayOperatorType(operatorType);
            item.setReplayOperatorId(String.valueOf(operatorId));
            item.setLastReplayAt(replayAt);
            item.setLastReplayResult("SUCCEEDED");
            item.setLastReplayError(null);
        });
    }

    public void markAuditDeadLetterReplayFailed(Long id, String replayKey, String operatorType, Long operatorId,
                                                String replayError, Instant replayAt) {
        findAuditDeadLetterById(id).ifPresent(item -> {
            item.setReplayStatus(InventoryAuditReplayStatus.FAILED);
            item.setReplayCount((item.getReplayCount() == null ? 0 : item.getReplayCount()) + 1);
            item.setReplayKey(replayKey);
            item.setReplayOperatorType(operatorType);
            item.setReplayOperatorId(String.valueOf(operatorId));
            item.setLastReplayAt(replayAt);
            item.setLastReplayResult("FAILED");
            item.setLastReplayError(replayError);
        });
    }

    public InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
        if (task.getId() == null) {
            task = new InventoryAuditReplayTask(auditReplayTaskIdGenerator.getAndIncrement(), task.getTenantIdValue(),
                    task.getTaskNoValue(), task.getStatus(), task.getTotalCount(), task.getProcessedCount(),
                    task.getSuccessCount(), task.getFailedCount(), task.getReplayKeyPrefix(), task.getOperatorType(),
                    task.getOperatorIdValue(), task.getProcessingOwner(), task.getLeaseUntil(), task.getLastError(),
                    task.getCreatedAt(), task.getStartedAt(), task.getPausedAt(), task.getFinishedAt(),
                    task.getUpdatedAt());
        }
        auditReplayTasks.put(task.getIdValue(), task);
        return task;
    }

    public void batchSaveAuditReplayTaskItems(Long taskId, Long tenantId, List<Long> deadLetterIds, Instant createdAt) {
        List<InventoryAuditReplayTaskItem> items = auditReplayTaskItems.computeIfAbsent(taskId, key -> new ArrayList<>());
        for (Long deadLetterId : deadLetterIds) {
            items.add(new InventoryAuditReplayTaskItem(auditReplayTaskItemIdGenerator.getAndIncrement(), taskId, tenantId,
                    deadLetterId, InventoryAuditReplayTaskItemStatus.PENDING.value(), null, null, null, null, null, createdAt));
        }
    }

    public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(Long taskId) {
        return Optional.ofNullable(auditReplayTasks.get(taskId));
    }

    public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(Instant now, int limit,
                                                                        String processingOwner, Instant leaseUntil) {
        return auditReplayTasks.values().stream()
                .filter(task -> InventoryAuditReplayTaskStatus.PENDING.equals(task.getStatus())
                        || InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task -> task.getLeaseUntil() == null || !task.getLeaseUntil().isAfter(now))
                .sorted(java.util.Comparator.comparing(InventoryAuditReplayTask::getCreatedAt)
                        .thenComparing(InventoryAuditReplayTask::getIdValue))
                .limit(limit)
                .peek(task -> {
                    task.setStatus(InventoryAuditReplayTaskStatus.RUNNING);
                    task.setProcessingOwner(processingOwner);
                    task.setLeaseUntil(leaseUntil);
                    if (task.getStartedAt() == null) {
                        task.setStartedAt(now);
                    }
                    task.setUpdatedAt(now);
                })
                .toList();
    }

    public void renewAuditReplayTaskLease(Long taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
        findAuditReplayTaskById(taskId)
                .filter(task -> InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task -> processingOwner.equals(task.getProcessingOwner()))
                .ifPresent(task -> {
                    task.setLeaseUntil(leaseUntil);
                    task.setUpdatedAt(updatedAt);
                });
    }

    public List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(Long taskId, int limit) {
        return auditReplayTaskItems.getOrDefault(taskId, List.of()).stream()
                .filter(item -> InventoryAuditReplayTaskItemStatus.PENDING.equals(item.getItemStatus()))
                .sorted(java.util.Comparator.comparing(InventoryAuditReplayTaskItem::getId))
                .limit(limit)
                .toList();
    }

    public void markAuditReplayTaskItemResult(Long itemId, InventoryAuditReplayTaskItemStatus itemStatus,
                                              InventoryAuditReplayStatus replayStatus,
                                              String replayKey, String resultMessage, Instant startedAt,
                                              Instant finishedAt) {
        auditReplayTaskItems.values().forEach(items -> items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    if (!InventoryAuditReplayTaskItemStatus.PENDING.equals(item.getItemStatus())) {
                        return;
                    }
                    item.setItemStatus(itemStatus);
                    item.setReplayStatus(replayStatus);
                    item.setReplayKey(replayKey);
                    item.setResultMessage(resultMessage);
                    item.setStartedAt(startedAt);
                    item.setFinishedAt(finishedAt);
                    item.setUpdatedAt(finishedAt);
                }));
    }

    public void incrementAuditReplayTaskProgress(Long taskId, String processingOwner, int processedDelta,
                                                 int successDelta, int failedDelta, Instant updatedAt) {
        findAuditReplayTaskById(taskId)
                .filter(task -> InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task -> processingOwner.equals(task.getProcessingOwner()))
                .ifPresent(task -> {
                    task.setProcessedCount((task.getProcessedCount() == null ? 0 : task.getProcessedCount())
                            + Math.max(processedDelta, 0));
                    task.setSuccessCount((task.getSuccessCount() == null ? 0 : task.getSuccessCount())
                            + Math.max(successDelta, 0));
                    task.setFailedCount((task.getFailedCount() == null ? 0 : task.getFailedCount())
                            + Math.max(failedDelta, 0));
                    task.setUpdatedAt(updatedAt);
                });
    }

    public void finishAuditReplayTask(Long taskId, String processingOwner, String status, String lastError,
                                      Instant finishedAt) {
        findAuditReplayTaskById(taskId)
                .filter(task -> InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task -> processingOwner.equals(task.getProcessingOwner()))
                .ifPresent(task -> {
                    task.setStatus(InventoryAuditReplayTaskStatus.fromValue(status));
                    task.setLastError(lastError);
                    task.setProcessingOwner(null);
                    task.setLeaseUntil(null);
                    task.setFinishedAt(finishedAt);
                    task.setUpdatedAt(finishedAt);
                });
    }

    public boolean pauseAuditReplayTask(Long taskId, Long tenantId, Long operatorId, Instant pausedAt) {
        return findAuditReplayTaskById(taskId)
                .filter(task -> tenantId.equals(task.getTenantIdValue()))
                .filter(task -> InventoryAuditReplayTaskStatus.PENDING.equals(task.getStatus())
                        || InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .map(task -> {
                    task.setStatus(InventoryAuditReplayTaskStatus.PAUSED);
                    task.setProcessingOwner(null);
                    task.setLeaseUntil(null);
                    task.setPausedAt(pausedAt);
                    task.setUpdatedAt(pausedAt);
                    return true;
                }).orElse(false);
    }

    public boolean resumeAuditReplayTask(Long taskId, Long tenantId, Long operatorId, Instant updatedAt) {
        return findAuditReplayTaskById(taskId)
                .filter(task -> tenantId.equals(task.getTenantIdValue()))
                .filter(task -> InventoryAuditReplayTaskStatus.PAUSED.equals(task.getStatus()))
                .map(task -> {
                    task.setStatus(InventoryAuditReplayTaskStatus.PENDING);
                    task.setPausedAt(null);
                    task.setUpdatedAt(updatedAt);
                    return true;
                }).orElse(false);
    }

    private static String key(Long tenantId, Long skuId) {
        return tenantId + ":" + skuId;
    }

    private static String reservationKey(Long tenantId, String orderNo) {
        return tenantId + ":" + orderNo;
    }

    private static String reservationKey(String tenantId, String orderNo) {
        return tenantId + ":" + orderNo;
    }

    private Optional<InventoryAuditOutbox> findAuditOutboxById(OutboxId outboxId) {
        return auditOutbox.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getId().equals(outboxId))
                .findFirst();
    }

    private boolean tryClaim(OutboxId outboxId, Instant now, String processingOwner, Instant leaseUntil) {
        return findAuditOutboxById(outboxId)
                .filter(item -> InventoryAuditOutboxStatus.NEW.equals(item.getStatus())
                        || InventoryAuditOutboxStatus.RETRYING.equals(item.getStatus()))
                .filter(item -> item.getNextRetryAt() == null || !item.getNextRetryAt().isAfter(now))
                .map(item -> {
                    item.setStatus(InventoryAuditOutboxStatus.PROCESSING);
                    item.setProcessingOwner(processingOwner);
                    item.setLeaseUntil(leaseUntil);
                    item.setClaimedAt(now);
                    item.setUpdatedAt(now);
                    return true;
                })
                .orElse(false);
    }

    private EventCode generateEventCode() {
        long id = auditOutboxEventCodeGenerator.getAndIncrement();
        String timestamp = LocalDateTime.now().format(EVENT_CODE_TIMESTAMP_FORMATTER);
        String suffix = String.format("%06d", Math.floorMod(id, 1_000_000L));
        return EventCode.of("EVT" + timestamp + "-" + suffix);
    }
}
