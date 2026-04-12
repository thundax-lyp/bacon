package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
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
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
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
    private final Map<Long, Long> auditReplayTaskTenants = new ConcurrentHashMap<>();
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
                .sorted(java.util.Comparator.comparing(inventory -> SkuIdCodec.toValue(inventory.getSkuId())))
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
        Version version = inventory.getVersion() == null
                ? new Version(0L)
                : inventory.getVersion().next();
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
        Long tenantId = BaconContextHolder.currentTenantId();
        java.util.Objects.requireNonNull(reservation.getId(), "reservation.id must not be null");
        reservation.getItems().forEach(item -> java.util.Objects.requireNonNull(item.getId(), "reservationItem.id must not be null"));
        reservations.put(
                reservationKey(
                        tenantId,
                        reservation.getOrderNo() == null
                                ? null
                                : reservation.getOrderNo().value()),
                reservation);
        return reservation;
    }

    public Optional<InventoryReservation> findReservation(OrderNo orderNo) {
        return Optional.ofNullable(reservations.get(
                reservationKey(BaconContextHolder.currentTenantId(), orderNo == null ? null : orderNo.value())));
    }

    public void saveLedger(InventoryLedger ledger) {
        java.util.Objects.requireNonNull(ledger.getId(), "ledger.id must not be null");
        ledgers.computeIfAbsent(
                        reservationKey(
                                BaconContextHolder.currentTenantId(),
                                ledger.getOrderNo() == null
                                        ? null
                                        : ledger.getOrderNo().value()),
                        key -> new ArrayList<>())
                .add(ledger);
    }

    public List<InventoryLedger> findLedgers(OrderNo orderNo) {
        return List.copyOf(ledgers.getOrDefault(
                reservationKey(BaconContextHolder.currentTenantId(), orderNo == null ? null : orderNo.value()),
                List.of()));
    }

    public void saveAuditLog(InventoryAuditLog auditLog) {
        java.util.Objects.requireNonNull(auditLog.getId(), "auditLog.id must not be null");
        auditLogs
                .computeIfAbsent(
                        reservationKey(
                                BaconContextHolder.currentTenantId(),
                                auditLog.getOrderNo() == null
                                        ? null
                                        : auditLog.getOrderNo().value()),
                        key -> new ArrayList<>())
                .add(auditLog);
    }

    public List<InventoryAuditLog> findAuditLogs(OrderNo orderNo) {
        return List.copyOf(auditLogs.getOrDefault(
                reservationKey(BaconContextHolder.currentTenantId(), orderNo == null ? null : orderNo.value()),
                List.of()));
    }

    public void saveAuditOutbox(InventoryAuditOutbox outbox) {
        String eventCode =
                outbox.getEventCode() == null ? null : outbox.getEventCode().value();
        if (eventCode == null) {
            eventCode = generateEventCode().value();
        }
        java.util.Objects.requireNonNull(outbox.getId(), "outbox.id must not be null");
        if (outbox.getEventCode() == null) {
            outbox.assignEventCode(EventCode.of(eventCode));
        }
        auditOutbox
                .computeIfAbsent(
                        reservationKey(
                                BaconContextHolder.currentTenantId(),
                                outbox.getOrderNo() == null
                                        ? null
                                        : outbox.getOrderNo().value()),
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
                        .thenComparing(item ->
                                item.getId() == null ? null : item.getId().value()))
                .limit(limit)
                .toList();
    }

    public List<InventoryAuditOutboxRepository.TenantScopedAuditOutbox> claimRetryableAuditOutbox(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        List<InventoryAuditOutboxRepository.TenantScopedAuditOutbox> claimed = new ArrayList<>(Math.max(limit, 0));
        List<InventoryAuditOutbox> candidates = findRetryableAuditOutbox(now, Math.max(limit * 3, limit));
        for (InventoryAuditOutbox candidate : candidates) {
            if (claimed.size() >= limit) {
                break;
            }
            if (!tryClaim(candidate.getId(), now, processingOwner, leaseUntil)) {
                continue;
            }
            findAuditOutboxById(candidate.getId())
                    .ifPresent(item -> claimed.add(new InventoryAuditOutboxRepository.TenantScopedAuditOutbox(
                            findAuditOutboxTenant(item), item)));
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
                item.releaseLeaseToRetrying(now);
                released++;
            }
        }
        return released;
    }

    public void updateAuditOutboxForRetry(
            OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        findAuditOutboxById(outboxId).ifPresent(item -> {
            item.markRetrying(retryCount, nextRetryAt, errorMessage, updatedAt);
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
                    item.markRetryingClaimed(retryCount, nextRetryAt, errorMessage, updatedAt);
                    return true;
                })
                .orElse(false);
    }

    public void markAuditOutboxDead(OutboxId outboxId, int retryCount, String deadReason, Instant updatedAt) {
        findAuditOutboxById(outboxId).ifPresent(item -> {
            item.markDead(retryCount, deadReason, updatedAt);
        });
    }

    public boolean markAuditOutboxDeadClaimed(
            OutboxId outboxId, String processingOwner, int retryCount, String deadReason, Instant updatedAt) {
        return findAuditOutboxById(outboxId)
                .filter(item -> InventoryAuditOutboxStatus.PROCESSING.equals(item.getStatus()))
                .filter(item -> processingOwner.equals(item.getProcessingOwner()))
                .map(item -> {
                    item.markDeadClaimed(retryCount, deadReason, updatedAt);
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
        Long tenantId = BaconContextHolder.currentTenantId();
        auditDeadLetters
                .computeIfAbsent(
                        reservationKey(
                                tenantId,
                                deadLetter.getOrderNo() == null
                                        ? null
                                        : deadLetter.getOrderNo().value()),
                        key -> new ArrayList<>())
                .add(deadLetter);
    }

    public List<InventoryAuditDeadLetter> pageAuditDeadLetters(
            OrderNo orderNo, InventoryAuditReplayStatus replayStatus, int pageNo, int pageSize) {
        TenantId tenantId = TenantId.of(BaconContextHolder.requireTenantId());
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> tenantId.equals(findAuditDeadLetterTenant(item)))
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

    public long countAuditDeadLetters(OrderNo orderNo, InventoryAuditReplayStatus replayStatus) {
        TenantId tenantId = TenantId.of(BaconContextHolder.requireTenantId());
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> tenantId.equals(findAuditDeadLetterTenant(item)))
                .filter(item -> orderNo == null || orderNo.equals(item.getOrderNo()))
                .filter(item -> replayStatus == null || replayStatus.equals(item.getReplayStatus()))
                .count();
    }

    public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(DeadLetterId id) {
        TenantId tenantId = TenantId.of(BaconContextHolder.requireTenantId());
        return auditDeadLetters.values().stream()
                .flatMap(List::stream)
                .filter(item -> tenantId.equals(findAuditDeadLetterTenant(item)))
                .filter(item -> java.util.Objects.equals(
                        item.getOutboxId() == null ? null : item.getOutboxId().value(), id == null ? null : id.value()))
                .findFirst();
    }

    public boolean claimAuditDeadLetterForReplay(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        TenantId tenantId = TenantId.of(BaconContextHolder.requireTenantId());
        return findAuditDeadLetterById(id)
                .filter(item -> tenantId.equals(findAuditDeadLetterTenant(item)))
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
            item.markReplaySucceeded(replayKey, operatorType, operatorId == null ? null : operatorId.value(), replayAt);
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
        java.util.Objects.requireNonNull(task.getId(), "replayTask.id must not be null");
        Long taskId = task.getId() == null ? null : task.getId().value();
        auditReplayTasks.put(taskId, task);
        auditReplayTaskTenants.put(taskId, BaconContextHolder.requireTenantId());
        return task;
    }

    public void batchSaveAuditReplayTaskItems(List<InventoryAuditReplayTaskItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        TaskId taskId = items.get(0).getTaskId();
        List<InventoryAuditReplayTaskItem> persistedItems =
                auditReplayTaskItems.computeIfAbsent(taskId == null ? null : taskId.value(), key -> new ArrayList<>());
        for (InventoryAuditReplayTaskItem item : items) {
            java.util.Objects.requireNonNull(item.getId(), "replayTaskItem.id must not be null");
            persistedItems.add(item);
        }
    }

    public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(TaskId taskId) {
        return Optional.ofNullable(auditReplayTasks.get(taskId == null ? null : taskId.value()));
    }

    public Long findAuditReplayTaskTenantId(TaskId taskId) {
        return auditReplayTaskTenants.get(taskId == null ? null : taskId.value());
    }

    public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return auditReplayTasks.values().stream()
                .filter(task -> InventoryAuditReplayTaskStatus.PENDING.equals(task.getStatus())
                        || InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task ->
                        task.getLeaseUntil() == null || !task.getLeaseUntil().isAfter(now))
                .sorted(java.util.Comparator.comparing(InventoryAuditReplayTask::getCreatedAt)
                        .thenComparing(task -> task.getId() == null ? null : task.getId().value()))
                .limit(limit)
                .peek(task -> task.claim(processingOwner, leaseUntil, now))
                .toList();
    }

    public void renewAuditReplayTaskLease(
            TaskId taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
        findAuditReplayTaskById(taskId)
                .filter(task -> InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task -> processingOwner.equals(task.getProcessingOwner()))
                .ifPresent(task -> task.renewLease(leaseUntil, updatedAt));
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
                    item.markResult(itemStatus, replayStatus, replayKey, resultMessage, startedAt, finishedAt);
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
                .ifPresent(task -> task.markItemProgress(processedDelta, successDelta, failedDelta, updatedAt));
    }

    public void finishAuditReplayTask(
            TaskId taskId, String processingOwner, String status, String lastError, Instant finishedAt) {
        findAuditReplayTaskById(taskId)
                .filter(task -> InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .filter(task -> processingOwner.equals(task.getProcessingOwner()))
                .ifPresent(task -> task.finish(InventoryAuditReplayTaskStatus.from(status), lastError, finishedAt));
    }

    public boolean pauseAuditReplayTask(TaskId taskId, OperatorId operatorId, Instant pausedAt) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return findAuditReplayTaskById(taskId)
                .filter(task -> java.util.Objects.equals(findAuditReplayTaskTenantId(task.getId()), tenantId))
                .filter(task -> InventoryAuditReplayTaskStatus.PENDING.equals(task.getStatus())
                        || InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                .map(task -> {
                    task.pause(pausedAt);
                    return true;
                })
                .orElse(false);
    }

    public boolean resumeAuditReplayTask(TaskId taskId, OperatorId operatorId, Instant updatedAt) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return findAuditReplayTaskById(taskId)
                .filter(task -> java.util.Objects.equals(findAuditReplayTaskTenantId(task.getId()), tenantId))
                .filter(task -> InventoryAuditReplayTaskStatus.PAUSED.equals(task.getStatus()))
                .map(task -> {
                    task.resume(updatedAt);
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

    private TenantId findAuditOutboxTenant(InventoryAuditOutbox target) {
        return auditOutbox.entrySet().stream()
                .filter(entry -> entry.getValue().contains(target))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .map(this::tenantIdFromReservationKey)
                .orElse(null);
    }

    private TenantId findAuditDeadLetterTenant(InventoryAuditDeadLetter target) {
        return auditDeadLetters.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(item -> item == target))
                .map(Map.Entry::getKey)
                .map(this::tenantIdFromReservationKey)
                .findFirst()
                .orElse(null);
    }

    private TenantId tenantIdFromReservationKey(String reservationKey) {
        int separator = reservationKey.indexOf(':');
        if (separator <= 0) {
            return null;
        }
        return TenantId.of(Long.parseLong(reservationKey.substring(0, separator)));
    }

    private boolean tryClaim(OutboxId outboxId, Instant now, String processingOwner, Instant leaseUntil) {
        return findAuditOutboxById(outboxId)
                .filter(item -> InventoryAuditOutboxStatus.NEW.equals(item.getStatus())
                        || InventoryAuditOutboxStatus.RETRYING.equals(item.getStatus()))
                .filter(item ->
                        item.getNextRetryAt() == null || !item.getNextRetryAt().isAfter(now))
                .map(item -> {
                    item.claim(processingOwner, leaseUntil, now);
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
