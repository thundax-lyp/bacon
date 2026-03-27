package com.github.thundax.bacon.storage.application.support;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 存储审计补偿重试调度器。
 */
@Component
public class StorageAuditOutboxRetryScheduler {

    private final StorageAuditOutboxRetryService storageAuditOutboxRetryService;

    public StorageAuditOutboxRetryScheduler(StorageAuditOutboxRetryService storageAuditOutboxRetryService) {
        this.storageAuditOutboxRetryService = storageAuditOutboxRetryService;
    }

    @Scheduled(fixedDelayString = "${bacon.storage.audit-retry.fixed-delay-millis:10000}")
    public void retryAuditOutbox() {
        storageAuditOutboxRetryService.retryOutbox();
        storageAuditOutboxRetryService.cleanupExpiredDeadOutbox();
    }
}
