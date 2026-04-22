package com.github.thundax.bacon.storage.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.test.logging.ExpectedLogCapture;
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

@ExtendWith(MockitoExtension.class)
class StorageAuditApplicationServiceTest {

    @Mock
    private IdGenerator idGenerator;

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
        when(idGenerator.nextId("storage_audit_log")).thenReturn(1001L);
        service = new StorageAuditApplicationService(
                idGenerator, storageAuditLogRepository, storageAuditOutboxRepository);
    }

    @AfterEach
    void tearDown() {
        Metrics.removeRegistry(meterRegistry);
        meterRegistry.close();
    }

    @Test
    void shouldIncrementSuccessMetricWhenAuditLogSaved() {
        service.record(
                TenantId.of(1L),
                StoredObjectId.of(100L),
                "GENERIC_ATTACHMENT",
                "owner-1",
                StorageAuditActionType.UPLOAD,
                null,
                "ACTIVE");

        verify(storageAuditLogRepository).insert(any());
        assertEquals(
                1.0d,
                meterRegistry
                        .get("bacon.storage.audit.write.success.total")
                        .tag("actionType", "UPLOAD")
                        .counter()
                        .count());
    }

    @Test
    void shouldPersistOutboxWhenAuditLogSaveFails() {
        doThrow(new IllegalStateException("force-fail-audit"))
                .when(storageAuditLogRepository)
                .insert(any());

        try (ExpectedLogCapture logs = ExpectedLogCapture.capture(StorageAuditApplicationService.class)) {
            service.record(
                    TenantId.of(1L),
                    StoredObjectId.of(100L),
                    "GENERIC_ATTACHMENT",
                    "owner-1",
                    StorageAuditActionType.UPLOAD,
                    null,
                    "ACTIVE");
            assertTrue(logs.contains("ALERT storage audit log write failed"));
            assertTrue(logs.contains("objectId=100"));
        }

        verify(storageAuditOutboxRepository).insert(any());
        assertEquals(
                1.0d,
                meterRegistry
                        .get("bacon.storage.audit.write.fail.total")
                        .tag("actionType", "UPLOAD")
                        .counter()
                        .count());
        assertEquals(
                1.0d,
                meterRegistry
                        .get("bacon.storage.audit.outbox.persist.success.total")
                        .tag("actionType", "UPLOAD")
                        .counter()
                        .count());
    }

    @Test
    void shouldIncrementOutboxFailMetricWhenOutboxPersistFails() {
        doThrow(new IllegalStateException("force-fail-audit"))
                .when(storageAuditLogRepository)
                .insert(any());
        doThrow(new IllegalStateException("force-fail-outbox"))
                .when(storageAuditOutboxRepository)
                .insert(any());

        try (ExpectedLogCapture logs = ExpectedLogCapture.capture(StorageAuditApplicationService.class)) {
            service.record(
                    TenantId.of(1L),
                    StoredObjectId.of(100L),
                    "GENERIC_ATTACHMENT",
                    "owner-1",
                    StorageAuditActionType.UPLOAD,
                    null,
                    "ACTIVE");
            assertTrue(logs.contains("ALERT storage audit log write failed"));
            assertTrue(logs.contains("ALERT storage audit outbox persist failed"));
        }

        assertEquals(
                1.0d,
                meterRegistry
                        .get("bacon.storage.audit.outbox.persist.fail.total")
                        .tag("actionType", "UPLOAD")
                        .counter()
                        .count());
    }
}
