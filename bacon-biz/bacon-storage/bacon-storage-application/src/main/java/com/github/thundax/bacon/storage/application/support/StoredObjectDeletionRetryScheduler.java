package com.github.thundax.bacon.storage.application.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 存储对象删除补偿调度器。
 */
@Slf4j
@Component
public class StoredObjectDeletionRetryScheduler {

    private final StoredObjectDeletionRetryService storedObjectDeletionRetryService;

    public StoredObjectDeletionRetryScheduler(StoredObjectDeletionRetryService storedObjectDeletionRetryService) {
        this.storedObjectDeletionRetryService = storedObjectDeletionRetryService;
    }

    @Scheduled(
            fixedDelayString = "${bacon.storage.deletion-retry.fixed-delay-millis:60000}",
            initialDelayString = "${bacon.storage.deletion-retry.fixed-delay-millis:60000}"
    )
    public void retryDeletingObjects() {
        int completedCount = storedObjectDeletionRetryService.retryDeletingObjects();
        if (completedCount > 0) {
            log.info("Stored object deletion retry batch finished, completedCount={}", completedCount);
        }
    }
}
