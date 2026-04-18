package com.github.thundax.bacon.storage.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.storage.application.config.StorageMultipartCleanupProperties;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.enums.UploadStatus;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MultipartUploadCleanupServiceTest {

    @Mock
    private MultipartUploadSessionRepository multipartUploadSessionRepository;

    @Mock
    private MultipartUploadPartRepository multipartUploadPartRepository;

    @Mock
    private StoredObjectStorageRepository storedObjectStorageRepository;

    private SimpleMeterRegistry meterRegistry;
    private StorageMultipartCleanupProperties properties;
    private MultipartUploadCleanupService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(meterRegistry);
        properties = new StorageMultipartCleanupProperties();
        properties.setEnabled(true);
        properties.setTimeoutSeconds(3600);
        properties.setBatchSize(100);
        service = new MultipartUploadCleanupService(
                multipartUploadSessionRepository,
                multipartUploadPartRepository,
                storedObjectStorageRepository,
                properties);
    }

    @AfterEach
    void tearDown() {
        Metrics.removeRegistry(meterRegistry);
        meterRegistry.close();
    }

    @Test
    void shouldAbortAndCleanupExpiredMultipartSessions() {
        MultipartUploadSession session = MultipartUploadSession.reconstruct(
                1L,
                "upload-expired",
                "GENERIC_ATTACHMENT",
                "owner-1",
                "attachment",
                "a.png",
                "image/png",
                "attachment/key.png",
                "provider-1",
                2048L,
                1024L,
                1,
                UploadStatus.UPLOADING,
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(7200),
                null,
                null);
        when(multipartUploadSessionRepository.listExpiredSessions(any(), any(), eq(100)))
                .thenReturn(List.of(session));
        when(multipartUploadSessionRepository.update(any(MultipartUploadSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int cleanedCount = service.cleanupExpiredSessions();

        assertEquals(1, cleanedCount);
        ArgumentCaptor<MultipartUploadSession> sessionCaptor = ArgumentCaptor.forClass(MultipartUploadSession.class);
        verify(multipartUploadSessionRepository).update(sessionCaptor.capture());
        assertEquals(UploadStatus.ABORTED, sessionCaptor.getValue().getUploadStatus());
        verify(storedObjectStorageRepository).delete(session);
        verify(multipartUploadPartRepository).deleteByUploadId("upload-expired");
        assertEquals(
                1.0d,
                meterRegistry
                        .get("bacon.storage.multipart.cleanup.success.total")
                        .tag("uploadStatus", UploadStatus.UPLOADING.value())
                        .counter()
                        .count());
    }

    @Test
    void shouldCleanupAlreadyAbortedSessionsWithoutSavingAgain() {
        MultipartUploadSession session = MultipartUploadSession.reconstruct(
                2L,
                "upload-aborted",
                "GENERIC_ATTACHMENT",
                "owner-2",
                "attachment",
                "b.png",
                "image/png",
                "attachment/key-b.png",
                "provider-2",
                2048L,
                1024L,
                1,
                UploadStatus.ABORTED,
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(7200),
                null,
                Instant.now().minusSeconds(7100));
        when(multipartUploadSessionRepository.listExpiredSessions(any(), any(), eq(100)))
                .thenAnswer(invocation -> {
                    List<String> statuses = invocation.getArgument(0);
                    assertTrue(statuses.contains(UploadStatus.ABORTED));
                    return List.of(session);
                });

        int cleanedCount = service.cleanupExpiredSessions();

        assertEquals(1, cleanedCount);
        verify(storedObjectStorageRepository).delete(session);
        verify(multipartUploadPartRepository).deleteByUploadId("upload-aborted");
        verify(multipartUploadSessionRepository, never()).update(any(MultipartUploadSession.class));
        assertEquals(
                1.0d,
                meterRegistry
                        .get("bacon.storage.multipart.cleanup.success.total")
                        .tag("uploadStatus", UploadStatus.ABORTED.value())
                        .counter()
                        .count());
    }

    @Test
    void shouldRecordCleanupFailureMetricWhenAbortFails() {
        MultipartUploadSession session = MultipartUploadSession.reconstruct(
                3L,
                "upload-failed",
                "GENERIC_ATTACHMENT",
                "owner-3",
                "attachment",
                "c.png",
                "image/png",
                "attachment/key-c.png",
                "provider-3",
                2048L,
                1024L,
                1,
                UploadStatus.UPLOADING,
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(7200),
                null,
                null);
        when(multipartUploadSessionRepository.listExpiredSessions(any(), any(), eq(100)))
                .thenReturn(List.of(session));
        org.mockito.Mockito.doThrow(new IllegalStateException("abort-fail"))
                .when(storedObjectStorageRepository)
                .delete(session);

        int cleanedCount = service.cleanupExpiredSessions();

        assertEquals(0, cleanedCount);
        assertEquals(
                1.0d,
                meterRegistry
                        .get("bacon.storage.multipart.cleanup.fail.total")
                        .tag("uploadStatus", UploadStatus.UPLOADING.value())
                        .counter()
                        .count());
    }

    @Test
    void shouldSkipCleanupWhenDisabled() {
        properties.setEnabled(false);

        int cleanedCount = service.cleanupExpiredSessions();

        assertEquals(0, cleanedCount);
        verify(multipartUploadSessionRepository, never()).listExpiredSessions(any(), any(), anyInt());
    }
}
