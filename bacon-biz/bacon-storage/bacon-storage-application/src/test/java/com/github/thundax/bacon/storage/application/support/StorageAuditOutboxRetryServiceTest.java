package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.application.config.StorageAuditRetryProperties;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditOutboxStatus;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditOutboxRepository;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageAuditOutboxRetryServiceTest {

    @Mock
    private StorageAuditLogRepository storageAuditLogRepository;
    @Mock
    private StorageAuditOutboxRepository storageAuditOutboxRepository;

    private SimpleMeterRegistry meterRegistry;
    private StorageAuditRetryProperties properties;
    private StorageAuditOutboxRetryService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(meterRegistry);
        properties = new StorageAuditRetryProperties();
        properties.setBatchSize(10);
        properties.setMaxRetries(2);
        properties.setBaseDelaySeconds(30L);
        properties.setMaxDelaySeconds(300L);
        service = new StorageAuditOutboxRetryService(storageAuditLogRepository, storageAuditOutboxRepository, properties);
    }

    @AfterEach
    void tearDown() {
        Metrics.removeRegistry(meterRegistry);
        meterRegistry.close();
    }

    @Test
    void shouldDeleteOutboxWhenRetrySucceeds() {
        StorageAuditOutbox item = outbox(100L, 0);
        when(storageAuditOutboxRepository.listRetryable(any(), any(), eq(10))).thenReturn(List.of(item));
        when(storageAuditOutboxRepository.claimForProcessing(eq(100L), any(), any(), any())).thenReturn(true);

        int processed = service.retryOutbox();

        assertEquals(1, processed);
        verify(storageAuditLogRepository).save(any());
        verify(storageAuditOutboxRepository).deleteById(100L);
        assertEquals(1.0d, meterRegistry.get("bacon.storage.audit.retry.success.total")
                .tag("actionType", "UPLOAD").counter().count());
    }

    @Test
    void shouldMarkRetryWhenRetryFailsBelowMaxRetries() {
        StorageAuditOutbox item = outbox(101L, 0);
        when(storageAuditOutboxRepository.listRetryable(any(), any(), eq(10))).thenReturn(List.of(item));
        when(storageAuditOutboxRepository.claimForProcessing(eq(101L), any(), any(), any())).thenReturn(true);
        doThrow(new IllegalStateException("retry-fail")).when(storageAuditLogRepository).save(any());

        service.retryOutbox();

        verify(storageAuditOutboxRepository).updateForRetry(eq(101L), eq(1), any(), eq("retry-fail"),
                eq(StorageAuditOutboxStatus.RETRYING), any());
        assertEquals(1.0d, meterRegistry.get("bacon.storage.audit.retry.fail.total")
                .tag("actionType", "UPLOAD").counter().count());
    }

    @Test
    void shouldMarkDeadWhenRetryExhausted() {
        StorageAuditOutbox item = outbox(102L, 2);
        when(storageAuditOutboxRepository.listRetryable(any(), any(), eq(10))).thenReturn(List.of(item));
        when(storageAuditOutboxRepository.claimForProcessing(eq(102L), any(), any(), any())).thenReturn(true);
        doThrow(new IllegalStateException("retry-fail")).when(storageAuditLogRepository).save(any());

        service.retryOutbox();

        verify(storageAuditOutboxRepository).markDead(eq(102L), eq(3), eq("retry-fail"), any());
        assertEquals(1.0d, meterRegistry.get("bacon.storage.audit.retry.dead.total")
                .tag("actionType", "UPLOAD").counter().count());
    }

    @Test
    void shouldCleanupExpiredDeadOutbox() {
        when(storageAuditOutboxRepository.deleteExpiredDead(any(), eq(100))).thenReturn(2);

        int deleted = service.cleanupExpiredDeadOutbox();

        assertEquals(2, deleted);
        assertEquals(2.0d, meterRegistry.get("bacon.storage.audit.cleanup.dead.total")
                .counter().count());
    }

    @Test
    void shouldSkipOutboxWhenClaimFails() {
        StorageAuditOutbox item = outbox(103L, 0);
        when(storageAuditOutboxRepository.listRetryable(any(), any(), eq(10))).thenReturn(List.of(item));
        when(storageAuditOutboxRepository.claimForProcessing(eq(103L), any(), any(), any())).thenReturn(false);

        int processed = service.retryOutbox();

        assertEquals(0, processed);
        verify(storageAuditLogRepository, org.mockito.Mockito.never()).save(any());
        verify(storageAuditOutboxRepository, org.mockito.Mockito.never()).deleteById(103L);
    }

    private StorageAuditOutbox outbox(Long id, int retryCount) {
        Instant now = Instant.parse("2026-03-27T12:00:00Z");
        return new StorageAuditOutbox(id, TenantId.of("tenant-a"), StoredObjectId.of("O100"), "GENERIC_ATTACHMENT",
                "owner-1", StorageAuditActionType.UPLOAD, null, "ACTIVE", "SYSTEM", 0L, now, "force-fail-audit",
                StorageAuditOutboxStatus.NEW, retryCount, now, now);
    }
}
