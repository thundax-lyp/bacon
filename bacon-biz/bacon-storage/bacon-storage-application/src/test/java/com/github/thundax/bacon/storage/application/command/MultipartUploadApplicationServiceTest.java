package com.github.thundax.bacon.storage.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.UploadStatus;
import com.github.thundax.bacon.storage.domain.model.valueobject.MultipartUploadStorageSession;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MultipartUploadApplicationServiceTest {

    @Mock
    private MultipartUploadSessionRepository multipartUploadSessionRepository;

    @Mock
    private MultipartUploadPartRepository multipartUploadPartRepository;

    @Mock
    private StoredObjectRepository storedObjectRepository;

    @Mock
    private StoredObjectStorageRepository storedObjectStorageRepository;

    @Mock
    private StorageAuditApplicationService storageAuditApplicationService;

    @Mock
    private StorageUploadLimitValidator storageUploadLimitValidator;

    @Mock
    private IdGenerator idGenerator;

    private MultipartUploadApplicationService service;

    @BeforeEach
    void setUp() {
        service = new MultipartUploadApplicationService(
                multipartUploadSessionRepository,
                multipartUploadPartRepository,
                storedObjectRepository,
                storedObjectStorageRepository,
                storageAuditApplicationService,
                storageUploadLimitValidator,
                idGenerator);
    }

    @Test
    void shouldPersistOwnershipWhenInitMultipartUpload() {
        when(storedObjectStorageRepository.initMultipartUpload("attachment", "a.png", "image/png"))
                .thenReturn(new MultipartUploadStorageSession("attachment/key.png", "provider-1"));
        when(idGenerator.nextId("storage_multipart_upload")).thenReturn(1001L);
        when(multipartUploadSessionRepository.insert(any(MultipartUploadSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.initMultipartUpload(new InitMultipartUploadCommand(
                "GENERIC_ATTACHMENT", "owner-1", "attachment", "a.png", "image/png", 1024L, 512L));

        assertEquals("owner-1", dto.getOwnerId());
        assertEquals("INITIATED", dto.getUploadStatus());
        assertTrue(dto.getUploadId().startsWith("storage"));
        assertTrue(dto.getUploadId().endsWith("-001001"));

        ArgumentCaptor<MultipartUploadSession> captor = ArgumentCaptor.forClass(MultipartUploadSession.class);
        verify(multipartUploadSessionRepository).insert(captor.capture());
        assertEquals(1001L, captor.getValue().getId());
        assertEquals("owner-1", captor.getValue().getOwnerId());
        assertTrue(captor.getValue().getUploadId().startsWith("storage"));
        assertTrue(captor.getValue().getUploadId().endsWith("-001001"));
    }

    @Test
    void shouldRejectInitWhenMultipartTotalSizeExceedsConfiguredLimit() {
        doThrow(new IllegalArgumentException("Multipart totalSize exceeds configured limit"))
                .when(storageUploadLimitValidator)
                .validateMultipartInit(2048L, 512L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.initMultipartUpload(new InitMultipartUploadCommand(
                        "GENERIC_ATTACHMENT", "owner-1", "attachment", "a.png", "image/png", 2048L, 512L)));
        verify(storedObjectStorageRepository, never()).initMultipartUpload(any(), any(), any());
    }

    @Test
    void shouldRejectMultipartPartUploadWhenOwnershipMismatch() {
        MultipartUploadSession session = MultipartUploadSession.reconstruct(
                1L,
                "1",
                "GENERIC_ATTACHMENT",
                "owner-1",
                "attachment",
                "a.png",
                "image/png",
                "attachment/key.png",
                "provider-1",
                1024L,
                512L,
                0,
                UploadStatus.INITIATED,
                Instant.now(),
                Instant.now(),
                null,
                null);
        when(multipartUploadSessionRepository.findByUploadId("1")).thenReturn(Optional.of(session));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadMultipartPart(new UploadMultipartPartCommand(
                        "1", "GENERIC_ATTACHMENT", "owner-2", 1, 512L, new ByteArrayInputStream(new byte[] {1}))));
        verify(storedObjectStorageRepository, never()).uploadPart(any(), any(), any(), any());
    }

    @Test
    void shouldRejectMultipartPartUploadWhenPartSizeExceedsConfiguredLimit() {
        MultipartUploadSession session = MultipartUploadSession.reconstruct(
                2L,
                "2",
                "GENERIC_ATTACHMENT",
                "owner-1",
                "attachment",
                "a.png",
                "image/png",
                "attachment/key.png",
                "provider-1",
                1024L,
                512L,
                0,
                UploadStatus.INITIATED,
                Instant.now(),
                Instant.now(),
                null,
                null);
        when(multipartUploadSessionRepository.findByUploadId("2")).thenReturn(Optional.of(session));
        doThrow(new IllegalArgumentException("Multipart part size exceeds configured limit"))
                .when(storageUploadLimitValidator)
                .validateMultipartPartUpload(session, 1024L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadMultipartPart(new UploadMultipartPartCommand(
                        "2", "GENERIC_ATTACHMENT", "owner-1", 1, 1024L, new ByteArrayInputStream(new byte[] {1}))));
        verify(storedObjectStorageRepository, never()).uploadPart(any(), any(), any(), any());
    }

    @Test
    void shouldNotIncrementUploadedPartCountWhenReuploadingSamePartNumber() {
        MultipartUploadSession session = MultipartUploadSession.reconstruct(
                3L,
                "3",
                "GENERIC_ATTACHMENT",
                "owner-1",
                "attachment",
                "a.png",
                "image/png",
                "attachment/key.png",
                "provider-1",
                1024L,
                512L,
                1,
                UploadStatus.UPLOADING,
                Instant.now(),
                Instant.now(),
                null,
                null);
        MultipartUploadPart existingPart =
                MultipartUploadPart.reconstruct(10L, "3", 1, "etag-old", 512L, Instant.now());
        when(multipartUploadSessionRepository.findByUploadId("3")).thenReturn(Optional.of(session));
        when(multipartUploadPartRepository.findByUploadIdAndPartNumber("3", 1))
                .thenReturn(Optional.of(existingPart), Optional.of(existingPart));
        when(storedObjectStorageRepository.uploadPart(eq(session), eq(1), eq(512L), org.mockito.ArgumentMatchers.any()))
                .thenReturn("etag-new");
        when(multipartUploadPartRepository.update(any(MultipartUploadPart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.uploadMultipartPart(new UploadMultipartPartCommand(
                "3", "GENERIC_ATTACHMENT", "owner-1", 1, 512L, new ByteArrayInputStream(new byte[] {1, 2, 3})));

        assertEquals(1, session.getUploadedPartCount());
        verify(multipartUploadSessionRepository, never()).update(any(MultipartUploadSession.class));
        ArgumentCaptor<MultipartUploadPart> captor = ArgumentCaptor.forClass(MultipartUploadPart.class);
        verify(multipartUploadPartRepository).update(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals("etag-new", captor.getValue().getEtag());
    }

    @Test
    void shouldRejectCompleteWhenMultipartIntegrityMismatch() {
        MultipartUploadSession session = MultipartUploadSession.reconstruct(
                2L,
                "2",
                "GENERIC_ATTACHMENT",
                "owner-1",
                "attachment",
                "a.png",
                "image/png",
                "attachment/key.png",
                "provider-1",
                1024L,
                512L,
                2,
                UploadStatus.UPLOADING,
                Instant.now(),
                Instant.now(),
                null,
                null);
        when(multipartUploadSessionRepository.findByUploadId("2")).thenReturn(Optional.of(session));
        when(multipartUploadPartRepository.listByUploadId("2"))
                .thenReturn(List.of(MultipartUploadPart.create(null, "2", 1, "etag-1", 512L, Instant.now())));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.completeMultipartUpload(
                        new CompleteMultipartUploadCommand("2", "GENERIC_ATTACHMENT", "owner-1")));
        verify(storedObjectStorageRepository, never()).completeMultipartUpload(any(), any());
    }

    @Test
    void shouldAbortMultipartUploadWithOwnershipValidation() {
        MultipartUploadSession session = MultipartUploadSession.reconstruct(
                5L,
                "5",
                "GENERIC_ATTACHMENT",
                "owner-1",
                "attachment",
                "a.png",
                "image/png",
                "attachment/key.png",
                "provider-1",
                1024L,
                512L,
                1,
                UploadStatus.UPLOADING,
                Instant.now(),
                Instant.now(),
                null,
                null);
        when(multipartUploadSessionRepository.findByUploadId("5")).thenReturn(Optional.of(session));
        when(multipartUploadSessionRepository.update(any(MultipartUploadSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.abortMultipartUpload(new AbortMultipartUploadCommand("5", "GENERIC_ATTACHMENT", "owner-1"));

        verify(storedObjectStorageRepository).abortMultipartUpload(session);
        verify(multipartUploadPartRepository).deleteByUploadId("5");
    }

    @Test
    void shouldCompleteMultipartUploadWhenOwnershipAndIntegrityPass() {
        MultipartUploadSession session = MultipartUploadSession.reconstruct(
                6L,
                "6",
                "GENERIC_ATTACHMENT",
                "owner-1",
                "attachment",
                "a.png",
                "image/png",
                "attachment/key.png",
                "provider-1",
                1024L,
                512L,
                2,
                UploadStatus.UPLOADING,
                Instant.now(),
                Instant.now(),
                null,
                null);
        when(multipartUploadSessionRepository.findByUploadId("6")).thenReturn(Optional.of(session));
        when(multipartUploadPartRepository.listByUploadId("6"))
                .thenReturn(List.of(
                        MultipartUploadPart.create(null, "6", 1, "etag-1", 512L, Instant.now()),
                        MultipartUploadPart.create(null, "6", 2, "etag-2", 512L, Instant.now())));
        when(idGenerator.nextId("stored-object-no")).thenReturn(123456L);
        when(idGenerator.nextId("stored-object-id")).thenReturn(100L);
        when(storedObjectStorageRepository.completeMultipartUpload(eq(session), any()))
                .thenReturn(new StoredObjectStorageResult(
                        StorageType.OSS, "bucket", "attachment/key.png", "http://test/key"));
        when(storedObjectRepository.insert(any(StoredObject.class))).thenAnswer(invocation -> {
            StoredObject input = invocation.getArgument(0);
            return StoredObject.reconstruct(
                    StoredObjectId.of(100L),
                    input.getStoredObjectNo(),
                    input.getStorageType(),
                    input.getBucketName(),
                    input.getObjectKey(),
                    input.getOriginalFilename(),
                    input.getContentType(),
                    input.getSize(),
                    input.getAccessEndpoint(),
                    input.getObjectStatus(),
                    input.getReferenceStatus());
        });
        when(multipartUploadSessionRepository.update(any(MultipartUploadSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.completeMultipartUpload(
                new CompleteMultipartUploadCommand("6", "GENERIC_ATTACHMENT", "owner-1"));

        assertNotNull(dto.getStoredObjectNo());
        assertTrue(dto.getStoredObjectNo().matches("^storage-\\d{14}-123456$"));
        verify(multipartUploadPartRepository).deleteByUploadId("6");
    }
}
