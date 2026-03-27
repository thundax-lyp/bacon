package com.github.thundax.bacon.storage.domain.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipartUploadSessionTest {

    @Test
    void shouldTrackUploadStateTransitions() {
        MultipartUploadSession session = MultipartUploadSession.initiate("upload-1", "tenant-a",
                "UPMS_USER_AVATAR", "avatar", "avatar.png", "image/png", 10_240L, 5_120L);

        session.recordUploadedPart();
        session.recordUploadedPart();
        assertEquals(MultipartUploadSession.STATUS_UPLOADING, session.getUploadStatus());
        assertEquals(2, session.getUploadedPartCount());

        session.markCompleted();
        assertTrue(session.isCompleted());
        assertThrows(IllegalStateException.class, session::markAborted);
    }

    @Test
    void shouldRejectInvalidPartAndAbortSession() {
        assertThrows(IllegalArgumentException.class, () -> MultipartUploadPart.create("u-1", 0,
                "etag-1", 1024L));
        assertThrows(IllegalArgumentException.class, () -> MultipartUploadPart.create("u-1", 1,
                "", 1024L));

        MultipartUploadSession session = MultipartUploadSession.initiate("upload-2", "tenant-a",
                "UPMS_USER_AVATAR", "avatar", "avatar.png", "image/png", 10_240L, 5_120L);
        session.markAborted();
        assertTrue(session.isAborted());
        assertThrows(IllegalStateException.class, session::recordUploadedPart);
    }
}
