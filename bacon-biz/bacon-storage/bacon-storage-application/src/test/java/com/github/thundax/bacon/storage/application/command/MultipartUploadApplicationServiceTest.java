package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.api.enums.UploadStatusEnum;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.valueobject.MultipartUploadStorageSession;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private MultipartUploadApplicationService service;

    @BeforeEach
    void setUp() {
        service = new MultipartUploadApplicationService(multipartUploadSessionRepository, multipartUploadPartRepository,
                storedObjectRepository, storedObjectStorageRepository, storageAuditApplicationService,
                storageUploadLimitValidator);
    }

    @Test
    void shouldPersistOwnershipWhenInitMultipartUpload() {
        when(storedObjectStorageRepository.initMultipartUpload("attachment", "a.png", "image/png"))
                .thenReturn(new MultipartUploadStorageSession("attachment/key.png", "provider-1"));
        when(multipartUploadSessionRepository.save(any(MultipartUploadSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.initMultipartUpload(new InitMultipartUploadCommand("GENERIC_ATTACHMENT", "owner-1",
                "tenant-a", "attachment", "a.png", "image/png", 1024L, 512L));

        assertEquals("owner-1", dto.getOwnerId());
        assertEquals("tenant-a", dto.getTenantId());
        assertEquals(UploadStatusEnum.INITIATED, dto.getUploadStatus());

        ArgumentCaptor<MultipartUploadSession> captor = ArgumentCaptor.forClass(MultipartUploadSession.class);
        verify(multipartUploadSessionRepository).save(captor.capture());
        assertEquals("owner-1", captor.getValue().getOwnerId());
    }

    @Test
    void shouldRejectInitWhenMultipartTotalSizeExceedsConfiguredLimit() {
        doThrow(new IllegalArgumentException("Multipart totalSize exceeds configured limit"))
                .when(storageUploadLimitValidator).validateMultipartInit(2048L, 512L);

        assertThrows(IllegalArgumentException.class,
                () -> service.initMultipartUpload(new InitMultipartUploadCommand("GENERIC_ATTACHMENT", "owner-1",
                        "tenant-a", "attachment", "a.png", "image/png", 2048L, 512L)));
        verify(storedObjectStorageRepository, never()).initMultipartUpload(any(), any(), any());
    }

    @Test
    void shouldRejectMultipartPartUploadWhenOwnershipMismatch() {
        MultipartUploadSession session = new MultipartUploadSession(1L, "upload-1", "tenant-a", "GENERIC_ATTACHMENT",
                "owner-1", "attachment", "a.png", "image/png", "attachment/key.png", "provider-1",
                1024L, 512L, 0, MultipartUploadSession.STATUS_INITIATED, Instant.now(), Instant.now(), null, null);
        when(multipartUploadSessionRepository.findByUploadId("upload-1")).thenReturn(Optional.of(session));

        assertThrows(IllegalArgumentException.class,
                () -> service.uploadMultipartPart(new UploadMultipartPartCommand("upload-1", "GENERIC_ATTACHMENT",
                        "owner-2", "tenant-a", 1, 512L, new ByteArrayInputStream(new byte[]{1}))));
        verify(storedObjectStorageRepository, never()).uploadPart(any(), any(), any(), any());
    }

    @Test
    void shouldRejectMultipartPartUploadWhenPartSizeExceedsConfiguredLimit() {
        MultipartUploadSession session = new MultipartUploadSession(1L, "upload-limit", "tenant-a", "GENERIC_ATTACHMENT",
                "owner-1", "attachment", "a.png", "image/png", "attachment/key.png", "provider-1",
                1024L, 512L, 0, MultipartUploadSession.STATUS_INITIATED, Instant.now(), Instant.now(), null, null);
        when(multipartUploadSessionRepository.findByUploadId("upload-limit")).thenReturn(Optional.of(session));
        doThrow(new IllegalArgumentException("Multipart part size exceeds configured limit"))
                .when(storageUploadLimitValidator).validateMultipartPartUpload(session, 1024L);

        assertThrows(IllegalArgumentException.class,
                () -> service.uploadMultipartPart(new UploadMultipartPartCommand("upload-limit", "GENERIC_ATTACHMENT",
                        "owner-1", "tenant-a", 1, 1024L, new ByteArrayInputStream(new byte[]{1}))));
        verify(storedObjectStorageRepository, never()).uploadPart(any(), any(), any(), any());
    }

    @Test
    void shouldNotIncrementUploadedPartCountWhenReuploadingSamePartNumber() {
        MultipartUploadSession session = new MultipartUploadSession(1L, "upload-retry", "tenant-a", "GENERIC_ATTACHMENT",
                "owner-1", "attachment", "a.png", "image/png", "attachment/key.png", "provider-1",
                1024L, 512L, 1, MultipartUploadSession.STATUS_UPLOADING, Instant.now(), Instant.now(), null, null);
        MultipartUploadPart existingPart = new MultipartUploadPart(10L, "upload-retry", 1, "etag-old", 512L, Instant.now());
        when(multipartUploadSessionRepository.findByUploadId("upload-retry")).thenReturn(Optional.of(session));
        when(multipartUploadPartRepository.findByUploadIdAndPartNumber("upload-retry", 1))
                .thenReturn(Optional.of(existingPart), Optional.of(existingPart));
        when(storedObjectStorageRepository.uploadPart(eq(session), eq(1), eq(512L), org.mockito.ArgumentMatchers.any()))
                .thenReturn("etag-new");
        when(multipartUploadPartRepository.save(any(MultipartUploadPart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.uploadMultipartPart(new UploadMultipartPartCommand("upload-retry", "GENERIC_ATTACHMENT",
                "owner-1", "tenant-a", 1, 512L, new ByteArrayInputStream(new byte[]{1, 2, 3})));

        assertEquals(1, session.getUploadedPartCount());
        verify(multipartUploadSessionRepository, never()).save(any(MultipartUploadSession.class));
        ArgumentCaptor<MultipartUploadPart> captor = ArgumentCaptor.forClass(MultipartUploadPart.class);
        verify(multipartUploadPartRepository).save(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals("etag-new", captor.getValue().getEtag());
    }

    @Test
    void shouldRejectCompleteWhenMultipartIntegrityMismatch() {
        MultipartUploadSession session = new MultipartUploadSession(1L, "upload-2", "tenant-a", "GENERIC_ATTACHMENT",
                "owner-1", "attachment", "a.png", "image/png", "attachment/key.png", "provider-1",
                1024L, 512L, 2, MultipartUploadSession.STATUS_UPLOADING, Instant.now(), Instant.now(), null, null);
        when(multipartUploadSessionRepository.findByUploadId("upload-2")).thenReturn(Optional.of(session));
        when(multipartUploadPartRepository.listByUploadId("upload-2")).thenReturn(List.of(
                MultipartUploadPart.create("upload-2", 1, "etag-1", 512L)));

        assertThrows(IllegalArgumentException.class,
                () -> service.completeMultipartUpload(new CompleteMultipartUploadCommand("upload-2",
                        "GENERIC_ATTACHMENT", "owner-1", "tenant-a")));
        verify(storedObjectStorageRepository, never()).completeMultipartUpload(any(), any());
    }

    @Test
    void shouldAbortMultipartUploadWithOwnershipValidation() {
        MultipartUploadSession session = new MultipartUploadSession(1L, "upload-3", "tenant-a", "GENERIC_ATTACHMENT",
                "owner-1", "attachment", "a.png", "image/png", "attachment/key.png", "provider-1",
                1024L, 512L, 1, MultipartUploadSession.STATUS_UPLOADING, Instant.now(), Instant.now(), null, null);
        when(multipartUploadSessionRepository.findByUploadId("upload-3")).thenReturn(Optional.of(session));
        when(multipartUploadSessionRepository.save(any(MultipartUploadSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.abortMultipartUpload(new AbortMultipartUploadCommand("upload-3", "GENERIC_ATTACHMENT",
                "owner-1", "tenant-a"));

        verify(storedObjectStorageRepository).abortMultipartUpload(session);
        verify(multipartUploadPartRepository).deleteByUploadId("upload-3");
    }

    @Test
    void shouldCompleteMultipartUploadWhenOwnershipAndIntegrityPass() {
        MultipartUploadSession session = new MultipartUploadSession(1L, "upload-4", "tenant-a", "GENERIC_ATTACHMENT",
                "owner-1", "attachment", "a.png", "image/png", "attachment/key.png", "provider-1",
                1024L, 512L, 2, MultipartUploadSession.STATUS_UPLOADING, Instant.now(), Instant.now(), null, null);
        when(multipartUploadSessionRepository.findByUploadId("upload-4")).thenReturn(Optional.of(session));
        when(multipartUploadPartRepository.listByUploadId("upload-4")).thenReturn(List.of(
                MultipartUploadPart.create("upload-4", 1, "etag-1", 512L),
                MultipartUploadPart.create("upload-4", 2, "etag-2", 512L)));
        when(storedObjectStorageRepository.completeMultipartUpload(eq(session), any()))
                .thenReturn(new StoredObjectStorageResult(StorageType.OSS, "bucket", "attachment/key.png",
                        "http://test/key"));
        when(storedObjectRepository.save(any(StoredObject.class))).thenAnswer(invocation -> {
            StoredObject input = invocation.getArgument(0);
            return new StoredObject(StoredObjectId.of("O100"), input.getTenantId(), input.getStorageType(), input.getBucketName(),
                    input.getObjectKey(), input.getOriginalFilename(), input.getContentType(), input.getSize(),
                    input.getAccessEndpoint(), input.getObjectStatus(), input.getReferenceStatus(), input.getCreatedBy(),
                    input.getCreatedAt(), input.getUpdatedBy(), input.getUpdatedAt());
        });
        when(multipartUploadSessionRepository.save(any(MultipartUploadSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.completeMultipartUpload(new CompleteMultipartUploadCommand("upload-4",
                "GENERIC_ATTACHMENT", "owner-1", "tenant-a"));

        assertEquals("O100", dto.getId());
        verify(multipartUploadPartRepository).deleteByUploadId("upload-4");
    }
}
