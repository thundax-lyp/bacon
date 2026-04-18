package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryAuditOutboxRetrier {

    private static final int MAX_EXPONENT = 20;
    private static final String DEAD_LETTER_ID_BIZ_TAG = "inventory-dead-letter-id";
    private static final String AUDIT_LOG_ID_BIZ_TAG = "inventory-audit-log-id";

    private final InventoryAuditRecordRepository inventoryAuditRecordRepository;
    private final InventoryAuditOutboxRepository inventoryAuditOutboxRepository;
    private final InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository;
    private final IdGenerator idGenerator;

    @Value("${bacon.inventory.audit.retry.batch-size:100}")
    private int batchSize;

    @Value("${bacon.inventory.audit.retry.enabled:true}")
    private boolean enabled;

    @Value("${bacon.inventory.audit.retry.max-retries:6}")
    private int maxRetries;

    @Value("${bacon.inventory.audit.retry.base-delay-seconds:30}")
    private long baseDelaySeconds;

    @Value("${bacon.inventory.audit.retry.max-delay-seconds:1800}")
    private long maxDelaySeconds;

    @Value("${bacon.inventory.audit.retry.lease-seconds:60}")
    private long leaseSeconds;

    @Value("${spring.application.name:bacon-inventory}")
    private String applicationName;

    private final String processingOwner = UUID.randomUUID().toString();

    public InventoryAuditOutboxRetrier(
            InventoryAuditRecordRepository inventoryAuditRecordRepository,
            InventoryAuditOutboxRepository inventoryAuditOutboxRepository,
            InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository,
            IdGenerator idGenerator) {
        this.inventoryAuditRecordRepository = inventoryAuditRecordRepository;
        this.inventoryAuditOutboxRepository = inventoryAuditOutboxRepository;
        this.inventoryAuditDeadLetterRepository = inventoryAuditDeadLetterRepository;
        this.idGenerator = idGenerator;
    }

    @Scheduled(fixedDelayString = "${bacon.inventory.audit.retry.fixed-delay-ms:10000}")
    public void retryAuditOutbox() {
        if (!enabled) {
            return;
        }
        Instant now = Instant.now();
        // 先释放过期租约，再按批次认领可重试 outbox，避免节点崩溃后审计事件长期卡死在 PROCESSING。
        int released = inventoryAuditOutboxRepository.releaseExpiredLease(now);
        if (released > 0) {
            Metrics.counter("bacon.inventory.audit.retry.lease.released.total").increment(released);
            log.warn("Released expired audit outbox leases, released={}", released);
        }
        int safeBatchSize = Math.max(batchSize, 1);
        Instant leaseUntil = now.plusSeconds(Math.max(leaseSeconds, 1L));
        String owner = applicationName + ":" + processingOwner;
        List<InventoryAuditOutboxRepository.TenantScopedAuditOutbox> outboxItems =
                inventoryAuditOutboxRepository.claimRetryable(now, safeBatchSize, owner, leaseUntil);
        for (InventoryAuditOutboxRepository.TenantScopedAuditOutbox scopedItem : outboxItems) {
            BaconContextHolder.runWithTenantId(
                    scopedItem.tenantId() == null ? null : scopedItem.tenantId().value(),
                    () -> retryOne(scopedItem, now, owner));
        }
    }

    private void retryOne(
            InventoryAuditOutboxRepository.TenantScopedAuditOutbox scopedItem, Instant now, String owner) {
        InventoryAuditOutbox item = scopedItem.outbox();
        try {
            // outbox 重试的目标很单一：把原始审计事件补写回正式审计表，成功后立即删除 outbox。
            inventoryAuditRecordRepository.insertLog(InventoryAuditLog.create(
                    idGenerator.nextId(AUDIT_LOG_ID_BIZ_TAG),
                    item.getOrderNo(),
                    item.getReservationNo(),
                    item.getActionType(),
                    item.getOperatorType(),
                    item.getOperatorId() == null ? null : OperatorId.of(item.getOperatorId()),
                    item.getOccurredAt()));
            if (!inventoryAuditOutboxRepository.deleteClaimed(item.getId(), owner)) {
                Metrics.counter("bacon.inventory.audit.retry.cas_conflict.total", "actionType", actionTypeValue(item))
                        .increment();
                log.warn(
                        "Inventory audit retry skip delete due to owner mismatch, outboxId={}, owner={}",
                        item.getId() == null ? null : item.getId().value(),
                        owner);
                return;
            }
            Metrics.counter("bacon.inventory.audit.retry.success.total", "actionType", actionTypeValue(item))
                    .increment();
        } catch (RuntimeException ex) {
            handleRetryFailure(scopedItem, now, ex, owner);
        }
    }

    private void handleRetryFailure(
            InventoryAuditOutboxRepository.TenantScopedAuditOutbox scopedItem,
            Instant now,
            RuntimeException ex,
            String owner) {
        InventoryAuditOutbox item = scopedItem.outbox();
        int nextRetryCount = (item.getRetryCount() == null ? 0 : item.getRetryCount()) + 1;
        String errorMessage = truncateMessage(ex.getMessage());
        // 超过重试上限后转死信，后续交给人工回放或回放任务处理，不再让定时任务无限重试。
        if (nextRetryCount > maxRetries) {
            String deadReason = "MAX_RETRIES_EXCEEDED";
            if (!inventoryAuditOutboxRepository.markDeadClaimed(
                    item.getId(), owner, nextRetryCount, deadReason, now)) {
                Metrics.counter("bacon.inventory.audit.retry.cas_conflict.total", "actionType", actionTypeValue(item))
                        .increment();
                log.warn(
                        "Inventory audit retry skip dead mark due to owner mismatch, outboxId={}, owner={}",
                        item.getId() == null ? null : item.getId().value(),
                        owner);
                return;
            }
            inventoryAuditDeadLetterRepository.insert(InventoryAuditDeadLetter.create(
                    DeadLetterId.of(idGenerator.nextId(DEAD_LETTER_ID_BIZ_TAG)),
                    item.getId(),
                    item.getEventCode(),
                    item.getOrderNo(),
                    item.getReservationNo(),
                    item.getActionType(),
                    item.getOperatorType(),
                    item.getOperatorId(),
                    item.getOccurredAt(),
                    nextRetryCount,
                    errorMessage,
                    deadReason,
                    now));
            Metrics.counter("bacon.inventory.audit.retry.dead.total", "actionType", actionTypeValue(item))
                    .increment();
            log.error(
                    "ALERT inventory audit retry exhausted, outboxId={}, orderNo={}, reservationNo={}, actionType={}",
                    item.getId() == null ? null : item.getId().value(),
                    item.getOrderNo() == null ? null : item.getOrderNo().value(),
                    item.getReservationNo() == null
                            ? null
                            : item.getReservationNo().value(),
                    actionTypeValue(item),
                    ex);
            return;
        }
        // 未到上限时采用指数退避，避免下游持久化异常时用固定频率持续放大故障。
        Instant nextRetryAt = now.plusSeconds(nextDelaySeconds(nextRetryCount));
        if (!inventoryAuditOutboxRepository.updateForRetryClaimed(
                item.getId(), owner, nextRetryCount, nextRetryAt, errorMessage, now)) {
            Metrics.counter("bacon.inventory.audit.retry.cas_conflict.total", "actionType", actionTypeValue(item))
                    .increment();
            log.warn(
                    "Inventory audit retry skip retry-mark due to owner mismatch, outboxId={}, owner={}",
                    item.getId() == null ? null : item.getId().value(),
                    owner);
            return;
        }
        Metrics.counter("bacon.inventory.audit.retry.fail.total", "actionType", actionTypeValue(item))
                .increment();
        log.warn(
                "Inventory audit retry failed, outboxId={}, orderNo={}, reservationNo={}, actionType={}, retryCount={}",
                item.getId() == null ? null : item.getId().value(),
                item.getOrderNo() == null ? null : item.getOrderNo().value(),
                item.getReservationNo() == null ? null : item.getReservationNo().value(),
                actionTypeValue(item),
                nextRetryCount,
                ex);
    }

    private String actionTypeValue(InventoryAuditOutbox item) {
        return item.getActionType() == null ? null : item.getActionType().value();
    }

    private long nextDelaySeconds(int retryCount) {
        long normalizedBaseDelay = Math.max(baseDelaySeconds, 1L);
        long normalizedMaxDelay = Math.max(maxDelaySeconds, normalizedBaseDelay);
        int exponent = Math.min(Math.max(retryCount - 1, 0), MAX_EXPONENT);
        long computed = normalizedBaseDelay * (1L << exponent);
        return Math.min(computed, normalizedMaxDelay);
    }

    private String truncateMessage(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
