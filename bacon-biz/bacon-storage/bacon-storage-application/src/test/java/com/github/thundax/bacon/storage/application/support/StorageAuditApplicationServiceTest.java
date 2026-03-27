package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
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

    private SimpleMeterRegistry meterRegistry;
    private StorageAuditApplicationService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(meterRegistry);
        service = new StorageAuditApplicationService(storageAuditLogRepository);
    }

    @AfterEach
    void tearDown() {
        Metrics.removeRegistry(meterRegistry);
        meterRegistry.close();
    }

    @Test
    void shouldIncrementSuccessMetricWhenAuditLogSaved() {
        service.record("tenant-a", 100L, "GENERIC_ATTACHMENT", "owner-1", "UPLOAD", null, "ACTIVE");

        verify(storageAuditLogRepository).save(any());
        assertEquals(1.0d, meterRegistry.get("bacon.storage.audit.write.success.total")
                .tag("actionType", "UPLOAD")
                .counter()
                .count());
    }

    @Test
    void shouldIncrementFailMetricWhenAuditLogSaveFails() {
        doThrow(new IllegalStateException("force-fail-audit")).when(storageAuditLogRepository).save(any());

        service.record("tenant-a", 100L, "GENERIC_ATTACHMENT", "owner-1", "UPLOAD", null, "ACTIVE");

        assertEquals(1.0d, meterRegistry.get("bacon.storage.audit.write.fail.total")
                .tag("actionType", "UPLOAD")
                .counter()
                .count());
    }
}
