package com.github.thundax.bacon.storage.interfaces.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.storage.api.request.AbortMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.CompleteMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.InitMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectDeleteFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectGetFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadMultipartPartFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadObjectFacadeRequest;
import com.github.thundax.bacon.storage.api.response.MultipartUploadPartFacadeResponse;
import com.github.thundax.bacon.storage.api.response.MultipartUploadSessionFacadeResponse;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.storage.application.command.StoredObjectCommandApplicationService;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StorageFacadeLocalContractTest {

    @Mock
    private StoredObjectCommandApplicationService storedObjectCommandApplicationService;

    @Mock
    private StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    @Test
    void shouldMapUploadAndMultipartFacadeToApplicationCommands() {
        when(storedObjectCommandApplicationService.uploadObject(argThat(command -> command != null
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.category().equals("attachment")
                        && command.originalFilename().equals("a.txt")
                        && command.contentType().equals("text/plain")
                        && command.size().equals(3L))))
                .thenReturn(objectDto("storage-1"));
        when(storedObjectCommandApplicationService.initMultipartUpload(argThat(command -> command != null
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.ownerId().equals("owner-1")
                        && command.category().equals("attachment")
                        && command.totalSize().equals(1024L)
                        && command.partSize().equals(512L))))
                .thenReturn(sessionDto());
        when(storedObjectCommandApplicationService.uploadMultipartPart(argThat(command -> command != null
                        && command.uploadId().equals("upload-1")
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.ownerId().equals("owner-1")
                        && command.partNumber().equals(1)
                        && command.size().equals(3L))))
                .thenReturn(new MultipartUploadPartDTO("upload-1", 1, "etag-1"));
        when(storedObjectCommandApplicationService.completeMultipartUpload(argThat(command -> command != null
                        && command.uploadId().equals("upload-1")
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.ownerId().equals("owner-1"))))
                .thenReturn(objectDto("storage-2"));
        StoredObjectFacadeLocalImpl facade =
                new StoredObjectFacadeLocalImpl(storedObjectCommandApplicationService, storedObjectQueryApplicationService);

        StoredObjectFacadeResponse uploaded = facade.uploadObject(new UploadObjectFacadeRequest(
                "GENERIC_ATTACHMENT", "attachment", "a.txt", "text/plain", 3L, new ByteArrayInputStream(new byte[] {1})));
        MultipartUploadSessionFacadeResponse session = facade.initMultipartUpload(new InitMultipartUploadFacadeRequest(
                "GENERIC_ATTACHMENT", "owner-1", "attachment", "a.txt", "text/plain", 1024L, 512L));
        MultipartUploadPartFacadeResponse part = facade.uploadMultipartPart(new UploadMultipartPartFacadeRequest(
                "upload-1", "GENERIC_ATTACHMENT", "owner-1", 1, 3L, new ByteArrayInputStream(new byte[] {1})));
        StoredObjectFacadeResponse completed = facade.completeMultipartUpload(
                new CompleteMultipartUploadFacadeRequest("upload-1", "GENERIC_ATTACHMENT", "owner-1"));

        assertThat(uploaded.getStoredObjectNo()).isEqualTo("storage-1");
        assertThat(session.getUploadStatus()).isEqualTo("INITIATED");
        assertThat(part.getEtag()).isEqualTo("etag-1");
        assertThat(completed.getStoredObjectNo()).isEqualTo("storage-2");
    }

    @Test
    void shouldMapReadReferenceAndDeleteFacadeToApplicationCommands() {
        when(storedObjectQueryApplicationService.getObjectByNo(argThat(query -> query != null
                        && query.storedObjectNo().value().equals("storage-1"))))
                .thenReturn(objectDto("storage-1"));
        StoredObjectFacadeLocalImpl facade =
                new StoredObjectFacadeLocalImpl(storedObjectCommandApplicationService, storedObjectQueryApplicationService);

        StoredObjectFacadeResponse object = facade.getObjectByNo(new StoredObjectGetFacadeRequest("storage-1"));
        facade.abortMultipartUpload(new AbortMultipartUploadFacadeRequest("upload-1", "GENERIC_ATTACHMENT", "owner-1"));
        facade.markObjectReferenced(new StoredObjectReferenceFacadeRequest("storage-1", "GENERIC_ATTACHMENT", "owner-1"));
        facade.clearObjectReference(new StoredObjectReferenceFacadeRequest("storage-1", "GENERIC_ATTACHMENT", "owner-1"));
        facade.deleteObject(new StoredObjectDeleteFacadeRequest("storage-1"));

        assertThat(object.getStoredObjectNo()).isEqualTo("storage-1");
        verify(storedObjectCommandApplicationService)
                .abortMultipartUpload(argThat(command -> command.uploadId().equals("upload-1")));
        verify(storedObjectCommandApplicationService)
                .markObjectReferenced(argThat(command -> command.storedObjectNo().value().equals("storage-1")
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.ownerId().equals("owner-1")));
        verify(storedObjectCommandApplicationService)
                .clearObjectReference(argThat(command -> command.storedObjectNo().value().equals("storage-1")));
        verify(storedObjectCommandApplicationService)
                .deleteObject(argThat(command -> command.storedObjectNo().value().equals("storage-1")));
    }

    private StoredObjectDTO objectDto(String storedObjectNo) {
        return new StoredObjectDTO(
                storedObjectNo,
                "LOCAL_FILE",
                "default",
                "attachment/a.txt",
                "a.txt",
                "text/plain",
                3L,
                "/files/attachment/a.txt",
                "ACTIVE",
                "UNREFERENCED",
                Instant.parse("2026-03-27T10:00:00Z"));
    }

    private MultipartUploadSessionDTO sessionDto() {
        return new MultipartUploadSessionDTO(
                "upload-1", "GENERIC_ATTACHMENT", "owner-1", "attachment", "a.txt", "text/plain", 1024L, 512L, 0,
                "INITIATED");
    }
}
