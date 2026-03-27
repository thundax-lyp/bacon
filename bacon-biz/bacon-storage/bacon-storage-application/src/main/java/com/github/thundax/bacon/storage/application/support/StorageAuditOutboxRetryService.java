package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.storage.application.config.StorageAuditRetryProperties;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditOutboxRepository;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 存储审计补偿重试服务。
 */
@Slf4j
@Service
public class StorageAuditOutboxRetryService {

    private static final int MAX_EXPONENT = 20;
    private static final List<String> RETRYABLE_STATUSES = List.of(
            StorageAuditOutbox.STATUS_NEW,
            StorageAuditOutbox.STATUS_RETRYING
    );

    private final StorageAuditLogRepository storageAuditLogRepository;
    private final StorageAuditOutboxRepository storageAuditOutboxRepository;
    private final StorageAuditRetryProperties properties;

    public StorageAuditOutboxRetryService(StorageAuditLogRepository storageAuditLogRepository,
                                          StorageAuditOutboxRepository storageAuditOutboxRepository,
                                          StorageAuditRetryProperties properties) {
        this.storageAuditLogRepository = storageAuditLogRepository;
        this.storageAuditOutboxRepository = storageAuditOutboxRepository;
        this.properties = properties;
    }

    public int retryOutbox() {
        if (!properties.isEnabled()) {
            return 0;
        }
        Instant now = Instant.now();
        List<StorageAuditOutbox> outboxItems = storageAuditOutboxRepository.listRetryable(
                RETRYABLE_STATUSES, now, Math.max(properties.getBatchSize(), 1));
        int processedCount = 0;
        for (StorageAuditOutbox item : outboxItems) {
            retryOne(item, now);
            processedCount++;
        }
        return processedCount;
    }

    protected void retryOne(StorageAuditOutbox item, Instant now) {
        try {
            storageAuditLogRepository.save(new StorageAuditLog(null, item.getTenantId(), item.getObjectId(),
                    item.getOwnerType(), item.getOwnerId(), item.getActionType(), item.getBeforeStatus(),
                    item.getAfterStatus(), item.getOperatorType(), item.getOperatorId(), item.getOccurredAt()));
            storageAuditOutboxRepository.deleteById(item.getId());
            Metrics.counter("bacon.storage.audit.retry.success.total", "actionType", item.getActionType()).increment();
        } catch (RuntimeException ex) {
            handleRetryFailure(item, now, ex);
        }
    }

    private void handleRetryFailure(StorageAuditOutbox item, Instant now, RuntimeException ex) {
        int nextRetryCount = (item.getRetryCount() == null ? 0 : item.getRetryCount()) + 1;
        String errorMessage = truncateMessage(ex.getMessage());
        if (nextRetryCount > properties.getMaxRetries()) {
            storageAuditOutboxRepository.markDead(item.getId(), nextRetryCount, errorMessage, now);
            Metrics.counter("bacon.storage.audit.retry.dead.total", "actionType", item.getActionType()).increment();
            log.error("ALERT storage audit retry exhausted, outboxId={}, objectId={}, actionType={}",
                    item.getId(), item.getObjectId(), item.getActionType(), ex);
            return;
        }
        Instant nextRetryAt = now.plusSeconds(nextDelaySeconds(nextRetryCount));
        storageAuditOutboxRepository.updateForRetry(item.getId(), nextRetryCount, nextRetryAt,
                errorMessage, StorageAuditOutbox.STATUS_RETRYING, now);
        Metrics.counter("bacon.storage.audit.retry.fail.total", "actionType", item.getActionType()).increment();
        log.warn("Storage audit retry failed, outboxId={}, objectId={}, actionType={}, retryCount={}",
                item.getId(), item.getObjectId(), item.getActionType(), nextRetryCount, ex);
    }

    private long nextDelaySeconds(int retryCount) {
        long normalizedBaseDelay = Math.max(properties.getBaseDelaySeconds(), 1L);
        long normalizedMaxDelay = Math.max(properties.getMaxDelaySeconds(), normalizedBaseDelay);
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
