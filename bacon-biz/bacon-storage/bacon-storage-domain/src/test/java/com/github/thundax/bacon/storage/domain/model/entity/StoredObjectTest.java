package com.github.thundax.bacon.storage.domain.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoredObjectTest {

    @Test
    void shouldBuildUploadedObjectWithDefaultStatuses() {
        StoredObject storedObject = StoredObject.newUploadedObject("tenant-a", "LOCAL_FILE", "default",
                "avatars/abc.png", "avatar.png", "image/png", 1024L, "/files/avatars/abc.png", 1001L);

        assertEquals(StoredObject.OBJECT_STATUS_ACTIVE, storedObject.getObjectStatus());
        assertEquals(StoredObject.REFERENCE_STATUS_UNREFERENCED, storedObject.getReferenceStatus());
        assertEquals(1001L, storedObject.getCreatedBy());
        assertEquals(1001L, storedObject.getUpdatedBy());
    }

    @Test
    void shouldSwitchReferenceAndDeleteStatus() {
        StoredObject storedObject = StoredObject.newUploadedObject("tenant-a", "LOCAL_FILE", "default",
                "avatars/abc.png", "avatar.png", "image/png", 1024L, "/files/avatars/abc.png", null);

        storedObject.markReferenced();
        assertTrue(storedObject.isReferenced());

        storedObject.markUnreferenced();
        assertEquals(StoredObject.REFERENCE_STATUS_UNREFERENCED, storedObject.getReferenceStatus());

        storedObject.markDeleted();
        assertTrue(storedObject.isDeleted());
    }
}
