package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StorageAuditOutboxDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StorageAuditOutboxMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageAuditOutboxRepositoryImplTest {

    @Mock
    private StorageAuditOutboxMapper storageAuditOutboxMapper;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private StorageAuditOutboxRepositoryImpl storageAuditOutboxRepository;

    @Test
    void shouldMapRetryableOutboxFromDataObject() {
        Instant retryBefore = Instant.parse("2026-03-27T10:00:00Z");
        StorageAuditOutboxDO outbox = buildOutboxDO(101L, StorageAuditOutbox.STATUS_NEW, retryBefore.minusSeconds(60));
        when(storageAuditOutboxMapper.selectList(any(Wrapper.class))).thenReturn(List.of(outbox));

        List<StorageAuditOutbox> result = storageAuditOutboxRepository.listRetryable(
                List.of(StorageAuditOutbox.STATUS_NEW, StorageAuditOutbox.STATUS_RETRYING), retryBefore, 50);

        verify(storageAuditOutboxMapper).selectList(any(Wrapper.class));
        assertThat(result).hasSize(1);
        StorageAuditOutbox mapped = result.get(0);
        assertThat(mapped.getId()).isEqualTo(101L);
        assertThat(mapped.getTenantId()).isEqualTo("tenant-a");
        assertThat(mapped.getObjectId()).isEqualTo(9001L);
        assertThat(mapped.getStatus()).isEqualTo(StorageAuditOutbox.STATUS_NEW);
        assertThat(mapped.getNextRetryAt()).isEqualTo(retryBefore.minusSeconds(60));
    }

    @Test
    void shouldUpdateRetryStateWithExpectedPayload() {
        Instant nextRetryAt = Instant.parse("2026-03-27T10:05:00Z");
        Instant updatedAt = Instant.parse("2026-03-27T10:01:00Z");
        ArgumentCaptor<StorageAuditOutboxDO> updateCaptor = ArgumentCaptor.forClass(StorageAuditOutboxDO.class);
        ArgumentCaptor<Wrapper<StorageAuditOutboxDO>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);

        storageAuditOutboxRepository.updateForRetry(201L, 2, nextRetryAt, "temporary-error",
                StorageAuditOutbox.STATUS_RETRYING, updatedAt);

        verify(storageAuditOutboxMapper).update(updateCaptor.capture(), wrapperCaptor.capture());
        StorageAuditOutboxDO update = updateCaptor.getValue();
        assertThat(update.getRetryCount()).isEqualTo(2);
        assertThat(update.getNextRetryAt()).isEqualTo(nextRetryAt);
        assertThat(update.getErrorMessage()).isEqualTo("temporary-error");
        assertThat(update.getStatus()).isEqualTo(StorageAuditOutbox.STATUS_RETRYING);
        assertThat(update.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(wrapperCaptor.getValue()).isNotNull();
    }

    @Test
    void shouldMarkOutboxDeadWithExpectedPayload() {
        Instant updatedAt = Instant.parse("2026-03-27T10:01:00Z");
        ArgumentCaptor<StorageAuditOutboxDO> updateCaptor = ArgumentCaptor.forClass(StorageAuditOutboxDO.class);
        ArgumentCaptor<Wrapper<StorageAuditOutboxDO>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);

        storageAuditOutboxRepository.markDead(301L, 5, "permanent-error", updatedAt);

        verify(storageAuditOutboxMapper).update(updateCaptor.capture(), wrapperCaptor.capture());
        StorageAuditOutboxDO update = updateCaptor.getValue();
        assertThat(update.getRetryCount()).isEqualTo(5);
        assertThat(update.getErrorMessage()).isEqualTo("permanent-error");
        assertThat(update.getStatus()).isEqualTo(StorageAuditOutbox.STATUS_DEAD);
        assertThat(update.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(wrapperCaptor.getValue()).isNotNull();
    }

    @Test
    void shouldDeleteExpiredDeadOutboxBySelectedIds() {
        Instant updatedBefore = Instant.parse("2026-03-27T10:00:00Z");
        when(storageAuditOutboxMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                buildOutboxDO(401L, StorageAuditOutbox.STATUS_DEAD, updatedBefore.minusSeconds(120)),
                buildOutboxDO(402L, StorageAuditOutbox.STATUS_DEAD, updatedBefore.minusSeconds(60))));
        when(storageAuditOutboxMapper.deleteByIds(List.of(401L, 402L))).thenReturn(2);

        int deleted = storageAuditOutboxRepository.deleteExpiredDead(updatedBefore, 20);

        verify(storageAuditOutboxMapper).selectList(any(Wrapper.class));
        verify(storageAuditOutboxMapper).deleteByIds(List.of(401L, 402L));
        assertThat(deleted).isEqualTo(2);
    }

    @Test
    void shouldSkipDeleteWhenNoExpiredDeadOutboxFound() {
        when(storageAuditOutboxMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        int deleted = storageAuditOutboxRepository.deleteExpiredDead(Instant.parse("2026-03-27T10:00:00Z"), 20);

        assertThat(deleted).isZero();
        verify(storageAuditOutboxMapper, never()).deleteByIds(any());
    }

    private StorageAuditOutboxDO buildOutboxDO(Long id, String status, Instant nextRetryAt) {
        StorageAuditOutboxDO dataObject = new StorageAuditOutboxDO();
        dataObject.setId(id);
        dataObject.setTenantId("tenant-a");
        dataObject.setObjectId(9001L);
        dataObject.setOwnerType("USER");
        dataObject.setOwnerId("U-1");
        dataObject.setActionType("UPLOAD");
        dataObject.setBeforeStatus("INITIATED");
        dataObject.setAfterStatus("ACTIVE");
        dataObject.setOperatorType("SYSTEM");
        dataObject.setOperatorId(1L);
        dataObject.setOccurredAt(Instant.parse("2026-03-27T09:00:00Z"));
        dataObject.setErrorMessage("retry-me");
        dataObject.setStatus(status);
        dataObject.setRetryCount(1);
        dataObject.setNextRetryAt(nextRetryAt);
        dataObject.setUpdatedAt(nextRetryAt);
        return dataObject;
    }
}
