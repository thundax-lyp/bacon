package com.github.thundax.bacon.storage.infra.persistence.repositoryimpl;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StorageAuditLogDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StorageAuditLogMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StorageAuditLogRepositoryImpl implements StorageAuditLogRepository {

    private static final String BIZ_TAG = "storage_audit_log";

    private final StorageAuditLogMapper storageAuditLogMapper;
    private final IdGenerator idGenerator;

    public StorageAuditLogRepositoryImpl(StorageAuditLogMapper storageAuditLogMapper, IdGenerator idGenerator) {
        this.storageAuditLogMapper = storageAuditLogMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public void save(StorageAuditLog storageAuditLog) {
        StorageAuditLogDO dataObject = new StorageAuditLogDO(idGenerator.nextId(BIZ_TAG), storageAuditLog.getTenantId(),
                storageAuditLog.getObjectId(), storageAuditLog.getOwnerType(), storageAuditLog.getOwnerId(),
                storageAuditLog.getActionType(), storageAuditLog.getBeforeStatus(), storageAuditLog.getAfterStatus(),
                storageAuditLog.getOperatorType(), storageAuditLog.getOperatorId(), storageAuditLog.getOccurredAt());
        storageAuditLogMapper.insert(dataObject);
    }
}
