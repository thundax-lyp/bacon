package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryAuditOutboxRetryService {

    private static final int MAX_EXPONENT = 20;

    private final InventoryLogRepository inventoryLogRepository;

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

    public InventoryAuditOutboxRetryService(InventoryLogRepository inventoryLogRepository) {
        this.inventoryLogRepository = inventoryLogRepository;
    }

    @Scheduled(fixedDelayString = "${bacon.inventory.audit.retry.fixed-delay-ms:10000}")
    public void retryAuditOutbox() {
        if (!enabled) {
            return;
        }
        Instant now = Instant.now();
        List<InventoryAuditOutbox> outboxItems = inventoryLogRepository.findRetryableAuditOutbox(now, Math.max(batchSize, 1));
        for (InventoryAuditOutbox item : outboxItems) {
            retryOne(item, now);
        }
    }

    private void retryOne(InventoryAuditOutbox item, Instant now) {
        try {
            inventoryLogRepository.saveAuditLog(new InventoryAuditLog(null, item.getTenantId(), item.getOrderNo(),
                    item.getReservationNo(), item.getActionType(), item.getOperatorType(), item.getOperatorId(),
                    item.getOccurredAt()));
            inventoryLogRepository.deleteAuditOutbox(item.getId());
            Metrics.counter("bacon.inventory.audit.retry.success.total", "actionType", item.getActionType()).increment();
        } catch (RuntimeException ex) {
            handleRetryFailure(item, now, ex);
        }
    }

    private void handleRetryFailure(InventoryAuditOutbox item, Instant now, RuntimeException ex) {
        int nextRetryCount = (item.getRetryCount() == null ? 0 : item.getRetryCount()) + 1;
        String errorMessage = truncateMessage(ex.getMessage());
        if (nextRetryCount > maxRetries) {
            String deadReason = "MAX_RETRIES_EXCEEDED";
            inventoryLogRepository.markAuditOutboxDead(item.getId(), nextRetryCount, deadReason, now);
            inventoryLogRepository.saveAuditDeadLetter(new InventoryAuditDeadLetter(null, item.getId(), item.getTenantId(),
                    item.getOrderNo(), item.getReservationNo(), item.getActionType(), item.getOperatorType(),
                    item.getOperatorId(), item.getOccurredAt(), nextRetryCount, errorMessage, deadReason, now));
            Metrics.counter("bacon.inventory.audit.retry.dead.total", "actionType", item.getActionType()).increment();
            log.error("ALERT inventory audit retry exhausted, outboxId={}, orderNo={}, reservationNo={}, actionType={}",
                    item.getId(), item.getOrderNo(), item.getReservationNo(), item.getActionType(), ex);
            return;
        }
        Instant nextRetryAt = now.plusSeconds(nextDelaySeconds(nextRetryCount));
        inventoryLogRepository.updateAuditOutboxForRetry(item.getId(), nextRetryCount, nextRetryAt, errorMessage, now);
        Metrics.counter("bacon.inventory.audit.retry.fail.total", "actionType", item.getActionType()).increment();
        log.warn("Inventory audit retry failed, outboxId={}, orderNo={}, reservationNo={}, actionType={}, retryCount={}",
                item.getId(), item.getOrderNo(), item.getReservationNo(), item.getActionType(), nextRetryCount, ex);
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
