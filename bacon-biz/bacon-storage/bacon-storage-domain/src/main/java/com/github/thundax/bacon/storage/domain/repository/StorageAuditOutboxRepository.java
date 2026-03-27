package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;

public interface StorageAuditOutboxRepository {

    void save(StorageAuditOutbox storageAuditOutbox);
}
