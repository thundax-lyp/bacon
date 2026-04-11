package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.mapper.SkuIdMapper;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.valueobject.Version;
import com.github.thundax.bacon.common.id.domain.OperatorId;
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
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservedQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test")
public class InMemoryInventoryRepositorySupport {

    private static final DateTimeFormatter EVENT_CODE_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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

    public Optional<Inventory> findInventory(TenantId tenantId, SkuId skuId) {
        return Optional.ofNullable(
                inventories.get(key(tenantId == null ? null : tenantId.value(), skuId == null ? null : skuId.value())));
    }

    public List<Inventory> findInventories(TenantId tenantId) {
        String tenantPrefix = tenantKeyPrefix(tenantId);
        return inventories.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(tenantPrefix))
                .map(Map.Entry::getValue)
                .sorted(java.util.Comparator.comparing(inventory -> SkuIdMapper.toValue(inventory.getSkuId())))
                .toList();
    }

    public List<Inventory> findInventories(TenantId tenantId, Set<SkuId> skuIds) {
        return skuIds.stream()
                .map(skuId -> inventories.get(
                        key(tenantId == null ? null : tenantId.value(), skuId == null ? null : skuId.value())))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public List<Inventory> pageInventories(
            TenantId tenantId, SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
        return findInventories(tenantId).stream()
                .filter(inventory -> skuId == null || java.util.Objects.equals(inventory.getSkuId(), skuId))
                .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    public long countInventories(TenantId tenantId, SkuId skuId, InventoryStatus status) {
        return findInventories(tenantId).stream()
                .filter(inventory -> skuId == null || java.util.Objects.equals(inventory.getSkuId(), skuId))
                .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                .count();
    }

    public Inventory saveInventory(Inventory inventory) {
        Long tenantId = BaconContextHolder.currentTenantId();
        if (inventory.getId() == null) {
            inventory = Inventory.reconstruct(
                    InventoryId.of(inventoryIdGenerator.getAndIncrement()),
                    inventory.getSkuId(),
                    inventory.getWarehouseCode(),
                    inventory.getOnHandQuantity(),
                    inventory.getReservedQuantity(),
                    inventory.getStatus(),
                    inventory.getVersion(),
                    inventory.getUpdatedAt());
        }
        Version version = inventory.getVersion() == null ? new Version(0L) : inventory.getVersion().next();
        inventory.markPersisted(version);
        inventories.put(
                key(
                        tenantId,
                        inventory.getSkuId() == null
                                ? null
                                : inventory.getSkuId().value()),
                inventory);
        return inventory;
    }

    public InventoryReservation saveReservation(InventoryReservation reservation) {
        if (reservation.getId() == null) {
            reservation = InventoryReservation.rehydrate(
                    reservationIdGenerator.getAndIncrement(),
                    reservation.getTenantId() == null
                            ? null
                            : reservation.getTenantId().value(),
                    reservation.getReservationNoValue(),
                    reservation.getOrderNoValue(),
                    reservation.getWarehouseCodeValue(),
                    reservation.getCreatedAt(),
                    reservation.getItems().stream()
                            .map(item -> new InventoryReservationItem(
                                    item.getId() == null ? itemIdGenerator.getAndIncrement() : item.getId(),
                                    item.getTenantId(),
                                    item.getReservationNo(),
                                    item.getSkuId(),
                                    item.getQuantity()))
                            .toList(),
                    reservation.getReservationStatusValue(),
                    reservation.getFailureReason(),
                    reservation.getReleaseReasonValue(),
                    reservation.getReleasedAt(),
                    reservation.getDeductedAt());
        }
        reservations.put(
                reservationKey(
                        reservation.getTenantId() == null
                                ? null
                                : reservation.getTenantId().value(),
                        reservation.getOrderNoValue()),
                reservation);
        return reservation;
    }

    public Optional<InventoryReservation> findReservation(TenantId tenantId, OrderNo orderNo) {
        return Optional.ofNullable(reservations.get(
                reservationKey(tenantId == null ? null : tenantId.value(), orderNo == null ? null : orderNo.value())));
    }

    public void saveLedger(InventoryLedger ledger) {
        if (ledger.getId() == null) {
            ledger = new InventoryLedger(
                    ledgerIdGenerator.getAndIncrement(),
                    ledger.getTenantId(),
                    ledger.getOrderNo(),
                    ledger.getReservationNo(),
                    ledger.getSkuId(),
                    ledger.getWarehouseCode(),
                    ledger.getLedgerType(),
                    ledger.getQuantity(),
                    ledger.getOccurredAt());
        }
        ledgers.computeIfAbsent(
                        reservationKey(
                                ledger.getTenantId() == null
                                        ? null
                                        : ledger.getTenantId().value(),
                                ledger.getOrderNoValue()),
                        key -> new ArrayList<>())
                .add(ledger);
    }

    public List<InventoryLedger> findLedgers(TenantId tenantId, OrderNo orderNo) {
        return List.copyOf(ledgers.getOrDefault(
                reservationKey(tenantId == null ? null : tenantId.value(), orderNo == null ? null : orderNo.value()),
                List.of()));
    }

    public void saveAuditLog(InventoryAuditLog auditLog) {
        if (auditLog.getId() == null) {
            auditLog = InventoryAuditLog.reconstruct(
                    auditLogIdGenerator.getAndIncrement(),
                    auditLog.getTenantId(),
                    auditLog.getOrderNo(),
                    auditLog.getReservationNo(),
                    auditLog.getActionType(),
                    auditLog.getOperatorType(),
                    auditLog.getOperatorId(),
                    auditLog.getOccurredAt());
        }
        auditLogs
                .computeIfAbsent(
                        reservationKey(
                                auditLog.getTenantId() == null
                                        ? null
                                        : auditLog.getTenantId().value(),
                                auditLog.getOrderNoValue()),
                        key -> new ArrayList<>())
                .add(auditLog);
    }

    public List<InventoryAuditLog> findAuditLogs(TenantId tenantId, OrderNo orderNo) {
        return List.copyOf(auditLogs.getOrDefault(
                reservationKey(tenantId == null ? null : tenantId.value(), orderNo == null ? null : orderNo.value()),
                List.of()));
    }

    public void saveAuditOutbox(InventoryAuditOutbox outbox) {
        String eventCode = outbox.getEventCodeValue();
        if (eventCode == null) {
            eventCode = generateEventCode().value();
        }
        if (outbox.getId() == null) {
            outbox = new InventoryAuditOutbox(
                    OutboxId.of(auditOutboxIdGenerator.getAndIncrement()),
                    outbox.getTenantId(),
                    EventCode.of(eventCode),
                    outbox.getOrderNo(),
                    outbox.getReservationNo(),
                    outbox.getActionType(),
                    outbox.getOperatorType(),
                    outbox.getOperatorId(),
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
        auditOutbox
                .computeIfAbsent(
                        reservationKey(
                                outbox.getTenantId() == null
                                        ? null
                                        : outbox.getTenantId().value(),
                                outbox.getOrderNoValue()),
                        key -> new ArrayList<>())
                .add(outbox);
    }

    public List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
        return auditOutbox.values().stream()
                .flatMap(List::stream)
                .filter(item -> InventoryAuditOutboxStatus.NEW.equals(item.getStatus())
                        || InventoryAuditOutboxStatus.RETRYING.equals(item.getStatus()))
                .filter(item ->
                        item.getNextRetryAt() == null || !item.getNextRetryAt().isAfter(now))
                .sorted(java.util.Comparator.comparing(InventoryAuditOutbox::getFailedAt)
                        .thenComparing(InventoryAuditOutbox::getIdValue))
                .limit(limit)
                .toList();
    }

    public List<InventoryAuditOutbox> claimRetryableAuditOutbox(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
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

    public void updateAuditOutboxForRetry(
            OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        findAuditOutboxById(outboxId).ifPresent(item -> {
            item.setStatus(InventoryAuditOutboxStatus.RETRYING);
            item.setRetryCount(retryCount);
            item.setNextRetryAt(nextRetryAt);
            item.setErrorMessage(errorMessage);
            item.setUpdatedAt(updatedAt);
        });
    }

    public boolean updateAuditOutboxForRetryClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            Instant updatedAt) {
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

    public boolean markAuditOutboxDeadClaimed(
            OutboxId outboxId, String processingOwner, int retryCount, String deadReason, Instant updatedAt) {
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
        auditDeadLetters
                .computeIfAbsent(
                        reservationKey(
                                deadLetter.getTenantId().value(),
                                deadLetter.getOrderNo().value()),
                        key -> new ArrayList<>())
                .add(deadLetter);
    }

    public List<InventoryAuditDeadLetter> pageAuditDeadLetters(
            TenantId tenantId, OrderNo orderNo, InventoryAuditReplayStatus replayStatus, int pageNo, int pageSize) {
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> tenantId.equals(item.getTenantId()))
                .filter(item -> orderNo == null || orderNo.equals(item.getOrderNo()))
                .filter(item -> replayStatus == null || replayStatus.equals(item.getReplayStatus()))
                .sorted(java.util.Comparator.comparing(InventoryAuditDeadLetter::getDeadAt)
                        .reversed()
                        .thenComparing(
                                item -> item.getOutboxId() == null
                                        ? null
                                        : item.getOutboxId().value(),
                                java.util.Comparator.reverseOrder()))
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    public long countAuditDeadLetters(TenantId tenantId, OrderNo orderNo, InventoryAuditReplayStatus replayStatus) {
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> tenantId.equals(item.getTenantId()))
                .filter(item -> orderNo == null || orderNo.equals(item.getOrderNo()))
                .filter(item -> replayStatus == null || replayStatus.equals(item.getReplayStatus()))
                .count();
    }

    public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(DeadLetterId id) {
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> java.util.Objects.equals(
                        item.getOutboxId() == null ? null : item.getOutboxId().value(), id == null ? null : id.value()))
                .findFirst();
    }

    public boolean claimAuditDeadLetterForReplay(
            DeadLetterId id,
            TenantId tenantId,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        return findAuditDeadLetterById(id)
                .filter(item -> tenantId.equals(item.getTenantId()))
                .filter(item -> InventoryAuditReplayStatus.PENDING.equals(item.getReplayStatus())
                        || InventoryAuditReplayStatus.FAILED.equals(item.getReplayStatus()))
                .map(item -> {
                    item.markReplayRunning(
                            replayKey, operatorType, operatorId == null ? null : operatorId.value(), replayAt);
                    return true;
                })
                .orElse(false);
    }

    public void markAuditDeadLetterReplaySuccess(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        findAuditDeadLetterById(id).ifPresent(item -> {
            item.markReplaySucceeded(
                    replayKey, operatorType, operatorId == null ? null : operatorId.value(), replayAt);
        });
    }

    public void markAuditDeadLetterReplayFailed(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            String replayError,
            Instant replayAt) {
        findAuditDeadLetterById(id).ifPresent(item -> {
            item.markReplayFailed(
                    replayKey, operatorType, operatorId == null ? null : operatorId.value(), replayError, replayAt);
        });
    }

    public InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
        if (task.getId() == null) {
            task = new InventoryAuditReplayTask(
                    TaskId.of(auditReplayTaskIdGenerator.getAndIncrement()),
                    task.getTenantId(),
                    task.getTaskNo(),
                    task.getStatus(),
                    task.getTotalCount(),
                    task.getProcessedCount(),
                    task.getSuccessCount(),
                    task.getFailedCount(),
                    task.getReplayKeyPrefix(),
                    task.getOperatorType(),
                    task.getOperatorId(),
                    task.getProcessingOwner(),
                    task.getLeaseUntil(),
                    task.getLastError(),
                    task.getCreatedAt(),
                    task.getStartedAt(),
                    task.getPausedAt(),
                    task.getFinishedAt(),
                    task.getUpdatedAt());
        }
        auditReplayTasks.put(task.getIdValue(), task);
        return task;
    }

    public void batchSaveAuditReplayTaskItems(
            TaskId taskId, TenantId tenantId, List<DeadLetterId> deadLetterIds, Instant createdAt) {
        List<InventoryAuditReplayTaskItem> items =
                auditReplayTaskItems.computeIfAbsent(taskId == null ? null : taskId.value(), key -> new ArrayList<>());
        for (DeadLetterId deadLetterId : deadLetterIds) {
            items.add(new InventoryAuditReplayTaskItem(
                    auditReplayTaskItemIdGenerator.getAndIncrement(),
                    tenantId,
                    taskId,
                    deadLetterId,
                    InventoryAuditReplayTaskItemStatus.PENDING,
                    null,
                    null,
                    null,
                    null,
                    null,
                    createdAt));
        }
    }

    public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(TaskId taskId) {
        return Optional.ofNullable(auditReplayTasks.get(taskId == null ? null : taskId.value()));
    }

    public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return auditReplayTasks.values().stream()
                .filter(task -> InventoryAuditReplayTaskStatus.PENDING.equals(task.getStatus())
                        || InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task ->
                        task.getLeaseUntil() == null || !task.getLeaseUntil().isAfter(now))
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

    public void renewAuditReplayTaskLease(
            TaskId taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
        findAuditReplayTaskById(taskId)
                .filter(task -> InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task -> processingOwner.equals(task.getProcessingOwner()))
                .ifPresent(task -> {
                    task.setLeaseUntil(leaseUntil);
                    task.setUpdatedAt(updatedAt);
                });
    }

    public List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(TaskId taskId, int limit) {
        return auditReplayTaskItems.getOrDefault(taskId == null ? null : taskId.value(), List.of()).stream()
                .filter(item -> InventoryAuditReplayTaskItemStatus.PENDING.equals(item.getItemStatus()))
                .sorted(java.util.Comparator.comparing(InventoryAuditReplayTaskItem::getId))
                .limit(limit)
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

    public void incrementAuditReplayTaskProgress(
            TaskId taskId,
            String processingOwner,
            int processedDelta,
            int successDelta,
            int failedDelta,
            Instant updatedAt) {
        findAuditReplayTaskById(taskId)
                .filter(task -> InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task -> processingOwner.equals(task.getProcessingOwner()))
                .ifPresent(task -> {
                    task.setProcessedCount((task.getProcessedCount() == null ? 0 : task.getProcessedCount())
                            + Math.max(processedDelta, 0));
                    task.setSuccessCount(
                            (task.getSuccessCount() == null ? 0 : task.getSuccessCount()) + Math.max(successDelta, 0));
                    task.setFailedCount(
                            (task.getFailedCount() == null ? 0 : task.getFailedCount()) + Math.max(failedDelta, 0));
                    task.setUpdatedAt(updatedAt);
                });
    }

    public void finishAuditReplayTask(
            TaskId taskId, String processingOwner, String status, String lastError, Instant finishedAt) {
        findAuditReplayTaskById(taskId)
                .filter(task -> InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task -> processingOwner.equals(task.getProcessingOwner()))
                .ifPresent(task -> {
                    task.setStatus(InventoryAuditReplayTaskStatus.from(status));
                    task.setLastError(lastError);
                    task.setProcessingOwner(null);
                    task.setLeaseUntil(null);
                    task.setFinishedAt(finishedAt);
                    task.setUpdatedAt(finishedAt);
                });
    }

    public boolean pauseAuditReplayTask(TaskId taskId, TenantId tenantId, OperatorId operatorId, Instant pausedAt) {
        return findAuditReplayTaskById(taskId)
                .filter(task -> java.util.Objects.equals(task.getTenantId(), tenantId))
                .filter(task -> InventoryAuditReplayTaskStatus.PENDING.equals(task.getStatus())
                        || InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .map(task -> {
                    task.setStatus(InventoryAuditReplayTaskStatus.PAUSED);
                    task.setProcessingOwner(null);
                    task.setLeaseUntil(null);
                    task.setPausedAt(pausedAt);
                    task.setUpdatedAt(pausedAt);
                    return true;
                })
                .orElse(false);
    }

    public boolean resumeAuditReplayTask(TaskId taskId, TenantId tenantId, OperatorId operatorId, Instant updatedAt) {
        return findAuditReplayTaskById(taskId)
                .filter(task -> java.util.Objects.equals(task.getTenantId(), tenantId))
                .filter(task -> InventoryAuditReplayTaskStatus.PAUSED.equals(task.getStatus()))
                .map(task -> {
                    task.setStatus(InventoryAuditReplayTaskStatus.PENDING);
                    task.setPausedAt(null);
                    task.setUpdatedAt(updatedAt);
                    return true;
                })
                .orElse(false);
    }

    private static String key(Long tenantId, Long skuId) {
        return tenantId + ":" + skuId;
    }

    private static String tenantKeyPrefix(TenantId tenantId) {
        return (tenantId == null ? "null" : tenantId.value()) + ":";
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
                .filter(item ->
                        item.getNextRetryAt() == null || !item.getNextRetryAt().isAfter(now))
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
