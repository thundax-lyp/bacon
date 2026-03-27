package com.github.thundax.bacon.storage.infra.repository.impl;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditOutboxRepository;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StorageAuditOutboxDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StorageAuditOutboxMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StorageAuditOutboxRepositoryImpl implements StorageAuditOutboxRepository {

    private static final String BIZ_TAG = "storage_audit_outbox";

    private final StorageAuditOutboxMapper storageAuditOutboxMapper;
    private final IdGenerator idGenerator;

    public StorageAuditOutboxRepositoryImpl(StorageAuditOutboxMapper storageAuditOutboxMapper, IdGenerator idGenerator) {
        this.storageAuditOutboxMapper = storageAuditOutboxMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public void save(StorageAuditOutbox storageAuditOutbox) {
        StorageAuditOutboxDO dataObject = new StorageAuditOutboxDO(idGenerator.nextId(BIZ_TAG),
                storageAuditOutbox.getTenantId(), storageAuditOutbox.getObjectId(), storageAuditOutbox.getOwnerType(),
                storageAuditOutbox.getOwnerId(), storageAuditOutbox.getActionType(), storageAuditOutbox.getBeforeStatus(),
                storageAuditOutbox.getAfterStatus(), storageAuditOutbox.getOperatorType(),
                storageAuditOutbox.getOperatorId(), storageAuditOutbox.getOccurredAt(),
                storageAuditOutbox.getErrorMessage(), storageAuditOutbox.getStatus(),
                storageAuditOutbox.getRetryCount(), storageAuditOutbox.getNextRetryAt(),
                storageAuditOutbox.getUpdatedAt());
        storageAuditOutboxMapper.insert(dataObject);
    }
}
