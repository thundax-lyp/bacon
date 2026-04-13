package com.github.thundax.bacon.storage.infra.repository.impl;

import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
import com.github.thundax.bacon.storage.infra.persistence.assembler.StorageAuditLogPersistenceAssembler;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StorageAuditLogDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StorageAuditLogMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StorageAuditLogRepositoryImpl implements StorageAuditLogRepository {

    private final StorageAuditLogMapper storageAuditLogMapper;

    public StorageAuditLogRepositoryImpl(StorageAuditLogMapper storageAuditLogMapper) {
        this.storageAuditLogMapper = storageAuditLogMapper;
    }

    @Override
    public void insert(StorageAuditLog storageAuditLog) {
        StorageAuditLogDO dataObject = StorageAuditLogPersistenceAssembler.toDataObject(storageAuditLog);
        storageAuditLogMapper.insert(dataObject);
    }
}
