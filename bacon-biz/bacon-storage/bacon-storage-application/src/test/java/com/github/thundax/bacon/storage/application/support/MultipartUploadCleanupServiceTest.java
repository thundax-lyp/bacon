package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.storage.application.config.StorageMultipartCleanupProperties;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultipartUploadCleanupServiceTest {

    @Mock
    private MultipartUploadSessionRepository multipartUploadSessionRepository;
    @Mock
    private MultipartUploadPartRepository multipartUploadPartRepository;
    @Mock
    private StoredObjectStorageRepository storedObjectStorageRepository;

    private StorageMultipartCleanupProperties properties;
    private MultipartUploadCleanupService service;

    @BeforeEach
    void setUp() {
        properties = new StorageMultipartCleanupProperties();
        properties.setEnabled(true);
        properties.setTimeoutSeconds(3600);
        properties.setBatchSize(100);
        service = new MultipartUploadCleanupService(multipartUploadSessionRepository, multipartUploadPartRepository,
                storedObjectStorageRepository, properties);
    }

    @Test
    void shouldAbortAndCleanupExpiredMultipartSessions() {
        MultipartUploadSession session = new MultipartUploadSession(1L, "upload-expired", "tenant-a",
                "GENERIC_ATTACHMENT", "owner-1", "attachment", "a.png", "image/png", "attachment/key.png",
                "provider-1", 2048L, 1024L, 1, MultipartUploadSession.STATUS_UPLOADING,
                Instant.now().minusSeconds(7200), Instant.now().minusSeconds(7200), null, null);
        when(multipartUploadSessionRepository.listExpiredSessions(any(), any(), eq(100)))
                .thenReturn(List.of(session));
        when(multipartUploadSessionRepository.save(any(MultipartUploadSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int cleanedCount = service.cleanupExpiredSessions();

        assertEquals(1, cleanedCount);
        ArgumentCaptor<MultipartUploadSession> sessionCaptor = ArgumentCaptor.forClass(MultipartUploadSession.class);
        verify(multipartUploadSessionRepository).save(sessionCaptor.capture());
        assertEquals(MultipartUploadSession.STATUS_ABORTED, sessionCaptor.getValue().getUploadStatus());
        verify(storedObjectStorageRepository).abortMultipartUpload(session);
        verify(multipartUploadPartRepository).deleteByUploadId("upload-expired");
    }

    @Test
    void shouldCleanupAlreadyAbortedSessionsWithoutSavingAgain() {
        MultipartUploadSession session = new MultipartUploadSession(2L, "upload-aborted", "tenant-a",
                "GENERIC_ATTACHMENT", "owner-2", "attachment", "b.png", "image/png", "attachment/key-b.png",
                "provider-2", 2048L, 1024L, 1, MultipartUploadSession.STATUS_ABORTED,
                Instant.now().minusSeconds(7200), Instant.now().minusSeconds(7200), null, Instant.now().minusSeconds(7100));
        when(multipartUploadSessionRepository.listExpiredSessions(any(), any(), eq(100)))
                .thenAnswer(invocation -> {
                    List<String> statuses = invocation.getArgument(0);
                    assertTrue(statuses.contains(MultipartUploadSession.STATUS_ABORTED));
                    return List.of(session);
                });

        int cleanedCount = service.cleanupExpiredSessions();

        assertEquals(1, cleanedCount);
        verify(storedObjectStorageRepository).abortMultipartUpload(session);
        verify(multipartUploadPartRepository).deleteByUploadId("upload-aborted");
        verify(multipartUploadSessionRepository, never()).save(any(MultipartUploadSession.class));
    }

    @Test
    void shouldSkipCleanupWhenDisabled() {
        properties.setEnabled(false);

        int cleanedCount = service.cleanupExpiredSessions();

        assertEquals(0, cleanedCount);
        verify(multipartUploadSessionRepository, never()).listExpiredSessions(any(), any(), anyInt());
    }
}
