package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditOutboxRepository;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StorageAuditOutboxDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StorageAuditOutboxMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

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

    @Override
    public List<StorageAuditOutbox> listRetryable(List<String> statuses, Instant retryBefore, int limit) {
        return storageAuditOutboxMapper.selectList(Wrappers.<StorageAuditOutboxDO>lambdaQuery()
                        .in(StorageAuditOutboxDO::getStatus, statuses)
                        .le(StorageAuditOutboxDO::getNextRetryAt, retryBefore)
                        .orderByAsc(StorageAuditOutboxDO::getNextRetryAt)
                        .last("limit " + limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        storageAuditOutboxMapper.deleteById(id);
    }

    @Override
    public void updateForRetry(Long id, int retryCount, Instant nextRetryAt, String errorMessage, String status,
                               Instant updatedAt) {
        StorageAuditOutboxDO update = new StorageAuditOutboxDO();
        update.setRetryCount(retryCount);
        update.setNextRetryAt(nextRetryAt);
        update.setErrorMessage(errorMessage);
        update.setStatus(status);
        update.setUpdatedAt(updatedAt);
        storageAuditOutboxMapper.update(update, Wrappers.<StorageAuditOutboxDO>lambdaUpdate()
                .eq(StorageAuditOutboxDO::getId, id));
    }

    @Override
    public void markDead(Long id, int retryCount, String errorMessage, Instant updatedAt) {
        StorageAuditOutboxDO update = new StorageAuditOutboxDO();
        update.setRetryCount(retryCount);
        update.setErrorMessage(errorMessage);
        update.setStatus(StorageAuditOutbox.STATUS_DEAD);
        update.setUpdatedAt(updatedAt);
        storageAuditOutboxMapper.update(update, Wrappers.<StorageAuditOutboxDO>lambdaUpdate()
                .eq(StorageAuditOutboxDO::getId, id));
    }

    @Override
    public int deleteExpiredDead(Instant updatedBefore, int limit) {
        List<Long> ids = storageAuditOutboxMapper.selectList(Wrappers.<StorageAuditOutboxDO>lambdaQuery()
                        .eq(StorageAuditOutboxDO::getStatus, StorageAuditOutbox.STATUS_DEAD)
                        .lt(StorageAuditOutboxDO::getUpdatedAt, updatedBefore)
                        .orderByAsc(StorageAuditOutboxDO::getUpdatedAt)
                        .last("limit " + limit))
                .stream()
                .map(StorageAuditOutboxDO::getId)
                .toList();
        if (ids.isEmpty()) {
            return 0;
        }
        return storageAuditOutboxMapper.deleteByIds(ids);
    }

    private StorageAuditOutbox toDomain(StorageAuditOutboxDO dataObject) {
        return new StorageAuditOutbox(dataObject.getId(), dataObject.getTenantId(), dataObject.getObjectId(),
                dataObject.getOwnerType(), dataObject.getOwnerId(), dataObject.getActionType(),
                dataObject.getBeforeStatus(), dataObject.getAfterStatus(), dataObject.getOperatorType(),
                dataObject.getOperatorId(), dataObject.getOccurredAt(), dataObject.getErrorMessage(),
                dataObject.getStatus(), dataObject.getRetryCount(), dataObject.getNextRetryAt(),
                dataObject.getUpdatedAt());
    }
}
