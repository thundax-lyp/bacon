package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditOutboxStatus;
import java.time.Instant;
import java.util.List;

public interface StorageAuditOutboxRepository {

    void insert(StorageAuditOutbox storageAuditOutbox);

    List<StorageAuditOutbox> findRetryable(List<StorageAuditOutboxStatus> statuses, Instant retryBefore, int limit);

    boolean claim(
            Long id, List<StorageAuditOutboxStatus> statuses, Instant retryBefore, Instant updatedAt);

    void deleteById(Long id);

    void updateForRetry(
            Long id,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            StorageAuditOutboxStatus status,
            Instant updatedAt);

    void markDead(Long id, int retryCount, String errorMessage, Instant updatedAt);

    int deleteExpired(Instant updatedBefore, int limit);
}
