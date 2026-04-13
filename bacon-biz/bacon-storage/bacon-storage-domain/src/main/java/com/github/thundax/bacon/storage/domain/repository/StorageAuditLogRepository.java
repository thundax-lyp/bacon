package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;

public interface StorageAuditLogRepository {

    void insert(StorageAuditLog storageAuditLog);
}
