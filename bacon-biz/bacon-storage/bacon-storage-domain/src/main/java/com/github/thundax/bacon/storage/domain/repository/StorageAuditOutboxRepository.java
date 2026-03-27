package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;

import java.time.Instant;
import java.util.List;

public interface StorageAuditOutboxRepository {

    void save(StorageAuditOutbox storageAuditOutbox);

    List<StorageAuditOutbox> listRetryable(List<String> statuses, Instant retryBefore, int limit);

    void deleteById(Long id);

    void updateForRetry(Long id, int retryCount, Instant nextRetryAt, String errorMessage, String status, Instant updatedAt);

    void markDead(Long id, int retryCount, String errorMessage, Instant updatedAt);

    int deleteExpiredDead(Instant updatedBefore, int limit);
}
