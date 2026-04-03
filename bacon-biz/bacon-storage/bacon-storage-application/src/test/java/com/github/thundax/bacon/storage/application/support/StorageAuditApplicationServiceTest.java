package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageAuditApplicationServiceTest {

    @Mock
    private StorageAuditLogRepository storageAuditLogRepository;
    @Mock
    private StorageAuditOutboxRepository storageAuditOutboxRepository;

    private SimpleMeterRegistry meterRegistry;
    private StorageAuditApplicationService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(meterRegistry);
        service = new StorageAuditApplicationService(storageAuditLogRepository, storageAuditOutboxRepository);
    }

    @AfterEach
    void tearDown() {
        Metrics.removeRegistry(meterRegistry);
        meterRegistry.close();
    }

    @Test
    void shouldIncrementSuccessMetricWhenAuditLogSaved() {
        service.record(TenantId.of("tenant-a"), StoredObjectId.of("O100"), "GENERIC_ATTACHMENT", "owner-1",
                StorageAuditActionType.UPLOAD, null, "ACTIVE");

        verify(storageAuditLogRepository).save(any());
        assertEquals(1.0d, meterRegistry.get("bacon.storage.audit.write.success.total")
                .tag("actionType", "UPLOAD")
                .counter()
                .count());
    }

    @Test
    void shouldPersistOutboxWhenAuditLogSaveFails() {
        doThrow(new IllegalStateException("force-fail-audit")).when(storageAuditLogRepository).save(any());

        service.record(TenantId.of("tenant-a"), StoredObjectId.of("O100"), "GENERIC_ATTACHMENT", "owner-1",
                StorageAuditActionType.UPLOAD, null, "ACTIVE");

        verify(storageAuditOutboxRepository).save(any());
        assertEquals(1.0d, meterRegistry.get("bacon.storage.audit.write.fail.total")
                .tag("actionType", "UPLOAD")
                .counter()
                .count());
        assertEquals(1.0d, meterRegistry.get("bacon.storage.audit.outbox.persist.success.total")
                .tag("actionType", "UPLOAD")
                .counter()
                .count());
    }

    @Test
    void shouldIncrementOutboxFailMetricWhenOutboxPersistFails() {
        doThrow(new IllegalStateException("force-fail-audit")).when(storageAuditLogRepository).save(any());
        doThrow(new IllegalStateException("force-fail-outbox")).when(storageAuditOutboxRepository).save(any());

        service.record(TenantId.of("tenant-a"), StoredObjectId.of("O100"), "GENERIC_ATTACHMENT", "owner-1",
                StorageAuditActionType.UPLOAD, null, "ACTIVE");

        assertEquals(1.0d, meterRegistry.get("bacon.storage.audit.outbox.persist.fail.total")
                .tag("actionType", "UPLOAD")
                .counter()
                .count());
    }
}
