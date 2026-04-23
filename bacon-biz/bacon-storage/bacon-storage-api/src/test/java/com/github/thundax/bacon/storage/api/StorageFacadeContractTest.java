package com.github.thundax.bacon.storage.api;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class StorageFacadeContractTest {

    @Test
    void shouldKeepUploadAndMultipartRequestContracts() throws Exception {
        assertField(UploadObjectFacadeRequest.class, "ownerType", String.class);
        assertField(UploadObjectFacadeRequest.class, "category", String.class);
        assertField(UploadObjectFacadeRequest.class, "originalFilename", String.class);
        assertField(UploadObjectFacadeRequest.class, "contentType", String.class);
        assertField(UploadObjectFacadeRequest.class, "size", Long.class);
        assertField(UploadObjectFacadeRequest.class, "inputStream", InputStream.class);
        assertField(InitMultipartUploadFacadeRequest.class, "ownerType", String.class);
        assertField(InitMultipartUploadFacadeRequest.class, "ownerId", String.class);
        assertField(InitMultipartUploadFacadeRequest.class, "category", String.class);
        assertField(InitMultipartUploadFacadeRequest.class, "originalFilename", String.class);
        assertField(InitMultipartUploadFacadeRequest.class, "contentType", String.class);
        assertField(InitMultipartUploadFacadeRequest.class, "totalSize", Long.class);
        assertField(InitMultipartUploadFacadeRequest.class, "partSize", Long.class);
        assertField(UploadMultipartPartFacadeRequest.class, "uploadId", String.class);
        assertField(UploadMultipartPartFacadeRequest.class, "ownerType", String.class);
        assertField(UploadMultipartPartFacadeRequest.class, "ownerId", String.class);
        assertField(UploadMultipartPartFacadeRequest.class, "partNumber", Integer.class);
        assertField(UploadMultipartPartFacadeRequest.class, "size", Long.class);
        assertField(UploadMultipartPartFacadeRequest.class, "inputStream", InputStream.class);

        UploadObjectFacadeRequest upload = new UploadObjectFacadeRequest(
                "GENERIC_ATTACHMENT", "attachment", "a.txt", "text/plain", 3L, new ByteArrayInputStream(new byte[0]));
        InitMultipartUploadFacadeRequest init = new InitMultipartUploadFacadeRequest(
                "GENERIC_ATTACHMENT", "owner-1", "attachment", "a.txt", "text/plain", 1024L, 512L);
        UploadMultipartPartFacadeRequest part = new UploadMultipartPartFacadeRequest(
                "upload-1", "GENERIC_ATTACHMENT", "owner-1", 1, 3L, new ByteArrayInputStream(new byte[0]));

        assertThat(upload.getOriginalFilename()).isEqualTo("a.txt");
        assertThat(init.getOwnerId()).isEqualTo("owner-1");
        assertThat(part.getPartNumber()).isEqualTo(1);
    }

    @Test
    void shouldKeepObjectCommandRequestContracts() throws Exception {
        assertField(CompleteMultipartUploadFacadeRequest.class, "uploadId", String.class);
        assertField(CompleteMultipartUploadFacadeRequest.class, "ownerType", String.class);
        assertField(CompleteMultipartUploadFacadeRequest.class, "ownerId", String.class);
        assertField(AbortMultipartUploadFacadeRequest.class, "uploadId", String.class);
        assertField(AbortMultipartUploadFacadeRequest.class, "ownerType", String.class);
        assertField(AbortMultipartUploadFacadeRequest.class, "ownerId", String.class);
        assertField(StoredObjectGetFacadeRequest.class, "storedObjectNo", String.class);
        assertField(StoredObjectReferenceFacadeRequest.class, "storedObjectNo", String.class);
        assertField(StoredObjectReferenceFacadeRequest.class, "ownerType", String.class);
        assertField(StoredObjectReferenceFacadeRequest.class, "ownerId", String.class);
        assertField(StoredObjectDeleteFacadeRequest.class, "storedObjectNo", String.class);

        assertThat(new CompleteMultipartUploadFacadeRequest("upload-1", "GENERIC_ATTACHMENT", "owner-1").getUploadId())
                .isEqualTo("upload-1");
        assertThat(new StoredObjectReferenceFacadeRequest("storage-1", "GENERIC_ATTACHMENT", "owner-1").getOwnerId())
                .isEqualTo("owner-1");
        assertThat(new StoredObjectDeleteFacadeRequest("storage-1").getStoredObjectNo()).isEqualTo("storage-1");
    }

    @Test
    void shouldKeepResponseContracts() throws Exception {
        assertField(StoredObjectFacadeResponse.class, "storedObjectNo", String.class);
        assertField(StoredObjectFacadeResponse.class, "storageType", String.class);
        assertField(StoredObjectFacadeResponse.class, "bucketName", String.class);
        assertField(StoredObjectFacadeResponse.class, "objectKey", String.class);
        assertField(StoredObjectFacadeResponse.class, "originalFilename", String.class);
        assertField(StoredObjectFacadeResponse.class, "contentType", String.class);
        assertField(StoredObjectFacadeResponse.class, "size", Long.class);
        assertField(StoredObjectFacadeResponse.class, "accessEndpoint", String.class);
        assertField(StoredObjectFacadeResponse.class, "objectStatus", String.class);
        assertField(StoredObjectFacadeResponse.class, "referenceStatus", String.class);
        assertField(StoredObjectFacadeResponse.class, "createdAt", Instant.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "uploadId", String.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "ownerType", String.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "ownerId", String.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "category", String.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "originalFilename", String.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "contentType", String.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "totalSize", Long.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "partSize", Long.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "uploadedPartCount", Integer.class);
        assertField(MultipartUploadSessionFacadeResponse.class, "uploadStatus", String.class);
        assertField(MultipartUploadPartFacadeResponse.class, "uploadId", String.class);
        assertField(MultipartUploadPartFacadeResponse.class, "partNumber", Integer.class);
        assertField(MultipartUploadPartFacadeResponse.class, "etag", String.class);

        StoredObjectFacadeResponse object = new StoredObjectFacadeResponse(
                "storage-1",
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
        MultipartUploadSessionFacadeResponse session = new MultipartUploadSessionFacadeResponse(
                "upload-1", "GENERIC_ATTACHMENT", "owner-1", "attachment", "a.txt", "text/plain", 1024L, 512L, 0,
                "INITIATED");
        MultipartUploadPartFacadeResponse part = new MultipartUploadPartFacadeResponse("upload-1", 1, "etag-1");

        assertThat(object.getStoredObjectNo()).isEqualTo("storage-1");
        assertThat(session.getUploadStatus()).isEqualTo("INITIATED");
        assertThat(part.getEtag()).isEqualTo("etag-1");
    }

    private void assertField(Class<?> type, String fieldName, Class<?> fieldType) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        assertThat(field.getType()).isEqualTo(fieldType);
    }
}
