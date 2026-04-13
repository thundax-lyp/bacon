package com.github.thundax.bacon.storage.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import org.junit.jupiter.api.Test;

class StoredObjectTest {

    @Test
    void shouldBuildUploadedObjectWithDefaultStatuses() {
        StoredObject storedObject = StoredObject.create(
                null,
                StorageType.LOCAL_FILE,
                "default",
                "avatars/abc.png",
                "avatar.png",
                "image/png",
                1024L,
                "/files/avatars/abc.png");

        assertEquals(StoredObjectStatus.ACTIVE, storedObject.getObjectStatus());
        assertEquals(StoredObjectReferenceStatus.UNREFERENCED, storedObject.getReferenceStatus());
    }

    @Test
    void shouldSwitchReferenceAndDeleteStatus() {
        StoredObject storedObject = StoredObject.create(
                null,
                StorageType.LOCAL_FILE,
                "default",
                "avatars/abc.png",
                "avatar.png",
                "image/png",
                1024L,
                "/files/avatars/abc.png");

        storedObject.markReferenced();
        assertTrue(storedObject.isReferenced());

        storedObject.markUnreferenced();
        assertEquals(StoredObjectReferenceStatus.UNREFERENCED, storedObject.getReferenceStatus());

        storedObject.markDeleting();
        assertTrue(storedObject.isDeleting());

        storedObject.markDeleted();
        assertTrue(storedObject.isDeleted());
    }

    @Test
    void shouldRejectReferenceMutationWhenObjectIsDeleting() {
        StoredObject storedObject = StoredObject.create(
                null,
                StorageType.LOCAL_FILE,
                "default",
                "avatars/abc.png",
                "avatar.png",
                "image/png",
                1024L,
                "/files/avatars/abc.png");

        storedObject.markDeleting();

        assertThrows(IllegalStateException.class, storedObject::markReferenced);
        assertThrows(IllegalStateException.class, storedObject::markUnreferenced);
    }
}
