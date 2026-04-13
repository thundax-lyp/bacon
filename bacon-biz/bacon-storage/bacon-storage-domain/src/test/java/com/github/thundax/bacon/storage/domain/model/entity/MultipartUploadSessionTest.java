package com.github.thundax.bacon.storage.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.storage.domain.model.enums.UploadStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

class MultipartUploadSessionTest {

    @Test
    void shouldTrackUploadStateTransitions() {
        MultipartUploadSession session = MultipartUploadSession.create(
                null,
                "upload-1",
                "GENERIC_ATTACHMENT",
                "owner-1",
                "attachment",
                "attachment.png",
                "image/png",
                "attachment/object-1.png",
                "provider-upload-1",
                10_240L,
                5_120L,
                java.time.Instant.now());

        session.recordUploadedPart();
        session.recordUploadedPart();
        assertEquals(UploadStatus.UPLOADING, session.getUploadStatus());
        assertEquals(2, session.getUploadedPartCount());
        assertEquals("owner-1", session.getOwnerId());
        assertEquals("attachment/object-1.png", session.getObjectKey());
        assertEquals("provider-upload-1", session.getProviderUploadId());

        session.markCompleted();
        assertTrue(session.isCompleted());
        assertThrows(IllegalStateException.class, session::markAborted);
    }

    @Test
    void shouldRejectInvalidPartAndAbortSession() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MultipartUploadPart.create(null, "u-1", 0, "etag-1", 1024L, java.time.Instant.now()));
        assertThrows(
                IllegalArgumentException.class,
                () -> MultipartUploadPart.create(null, "u-1", 1, "", 1024L, java.time.Instant.now()));

        MultipartUploadSession session = MultipartUploadSession.create(
                null,
                "upload-2",
                "GENERIC_ATTACHMENT",
                "owner-2",
                "attachment",
                "attachment.png",
                "image/png",
                "attachment/object-2.png",
                null,
                10_240L,
                5_120L,
                java.time.Instant.now());
        session.markAborted();
        assertTrue(session.isAborted());
        assertThrows(IllegalStateException.class, session::recordUploadedPart);
    }

    @Test
    void shouldValidateOwnershipAndMultipartIntegrity() {
        MultipartUploadSession session = MultipartUploadSession.create(
                null,
                "upload-3",
                "GENERIC_ATTACHMENT",
                "owner-3",
                "attachment",
                "attachment.png",
                "image/png",
                "attachment/object-3.png",
                "provider-upload-3",
                10_240L,
                5_120L,
                java.time.Instant.now());
        session.recordUploadedPart();
        session.recordUploadedPart();

        session.assertOwnership("GENERIC_ATTACHMENT", "owner-3");
        assertThrows(
                IllegalArgumentException.class,
                () -> session.assertOwnership("GENERIC_ATTACHMENT", "owner-4"));

        List<MultipartUploadPart> validParts = List.of(
                MultipartUploadPart.create(null, "upload-3", 1, "etag-1", 5_120L, java.time.Instant.now()),
                MultipartUploadPart.create(null, "upload-3", 2, "etag-2", 5_120L, java.time.Instant.now()));
        session.assertCompletable(validParts);

        List<MultipartUploadPart> invalidParts = List.of(
                MultipartUploadPart.create(null, "upload-3", 1, "etag-1", 5_120L, java.time.Instant.now()),
                MultipartUploadPart.create(null, "upload-3", 3, "etag-3", 5_120L, java.time.Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> session.assertCompletable(invalidParts));
    }
}
