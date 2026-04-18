package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditOutboxStatus;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditOutboxRepository;
import com.github.thundax.bacon.storage.infra.persistence.assembler.StorageAuditOutboxPersistenceAssembler;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StorageAuditOutboxDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StorageAuditOutboxMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class StorageAuditOutboxRepositoryImpl implements StorageAuditOutboxRepository {

    private final StorageAuditOutboxMapper storageAuditOutboxMapper;

    public StorageAuditOutboxRepositoryImpl(StorageAuditOutboxMapper storageAuditOutboxMapper) {
        this.storageAuditOutboxMapper = storageAuditOutboxMapper;
    }

    @Override
    public void insert(StorageAuditOutbox storageAuditOutbox) {
        StorageAuditOutboxDO dataObject = StorageAuditOutboxPersistenceAssembler.toDataObject(storageAuditOutbox);
        storageAuditOutboxMapper.insert(dataObject);
    }

    @Override
    public List<StorageAuditOutbox> findRetryable(
            List<StorageAuditOutboxStatus> statuses, Instant retryBefore, int limit) {
        return storageAuditOutboxMapper
                .selectList(Wrappers.<StorageAuditOutboxDO>lambdaQuery()
                        .in(StorageAuditOutboxDO::getStatus, toValues(statuses))
                        .le(StorageAuditOutboxDO::getNextRetryAt, retryBefore)
                        .orderByAsc(StorageAuditOutboxDO::getNextRetryAt)
                        .last("limit " + limit))
                .stream()
                .map(StorageAuditOutboxPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public boolean claim(
            Long id, List<StorageAuditOutboxStatus> statuses, Instant retryBefore, Instant updatedAt) {
        StorageAuditOutboxDO update = new StorageAuditOutboxDO();
        update.setStatus(StorageAuditOutboxStatus.PROCESSING.value());
        update.setUpdatedAt(updatedAt);
        return storageAuditOutboxMapper.update(
                        update,
                        Wrappers.<StorageAuditOutboxDO>lambdaUpdate()
                                .eq(StorageAuditOutboxDO::getId, id)
                                .in(StorageAuditOutboxDO::getStatus, toValues(statuses))
                                .le(StorageAuditOutboxDO::getNextRetryAt, retryBefore))
                > 0;
    }

    @Override
    public void deleteById(Long id) {
        storageAuditOutboxMapper.deleteById(id);
    }

    @Override
    public void updateForRetry(
            Long id,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            StorageAuditOutboxStatus status,
            Instant updatedAt) {
        StorageAuditOutboxDO update = new StorageAuditOutboxDO();
        update.setRetryCount(retryCount);
        update.setNextRetryAt(nextRetryAt);
        update.setErrorMessage(errorMessage);
        update.setStatus(status.value());
        update.setUpdatedAt(updatedAt);
        storageAuditOutboxMapper.update(
                update, Wrappers.<StorageAuditOutboxDO>lambdaUpdate().eq(StorageAuditOutboxDO::getId, id));
    }

    @Override
    public void markDead(Long id, int retryCount, String errorMessage, Instant updatedAt) {
        StorageAuditOutboxDO update = new StorageAuditOutboxDO();
        update.setRetryCount(retryCount);
        update.setErrorMessage(errorMessage);
        update.setStatus(StorageAuditOutboxStatus.DEAD.value());
        update.setUpdatedAt(updatedAt);
        storageAuditOutboxMapper.update(
                update, Wrappers.<StorageAuditOutboxDO>lambdaUpdate().eq(StorageAuditOutboxDO::getId, id));
    }

    @Override
    public int deleteExpired(Instant updatedBefore, int limit) {
        List<Long> ids = storageAuditOutboxMapper
                .selectList(Wrappers.<StorageAuditOutboxDO>lambdaQuery()
                        .eq(StorageAuditOutboxDO::getStatus, StorageAuditOutboxStatus.DEAD.value())
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

    private List<String> toValues(List<StorageAuditOutboxStatus> statuses) {
        return statuses.stream().map(StorageAuditOutboxStatus::value).toList();
    }
}
