package com.github.thundax.bacon.storage.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoredObjectTest {

    @Test
    void shouldBuildUploadedObjectWithDefaultStatuses() {
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of(1L), StorageType.LOCAL_FILE,
                "default", "avatars/abc.png", "avatar.png", "image/png", 1024L, "/files/avatars/abc.png", "u-1001");

        assertEquals(StoredObjectStatus.ACTIVE, storedObject.getObjectStatus());
        assertEquals(StoredObjectReferenceStatus.UNREFERENCED, storedObject.getReferenceStatus());
        assertEquals("u-1001", storedObject.getCreatedBy());
        assertEquals("u-1001", storedObject.getUpdatedBy());
    }

    @Test
    void shouldSwitchReferenceAndDeleteStatus() {
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of(1L), StorageType.LOCAL_FILE, "default",
                "avatars/abc.png", "avatar.png", "image/png", 1024L, "/files/avatars/abc.png", null);

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
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of(1L), StorageType.LOCAL_FILE, "default",
                "avatars/abc.png", "avatar.png", "image/png", 1024L, "/files/avatars/abc.png", null);

        storedObject.markDeleting();

        assertThrows(IllegalStateException.class, storedObject::markReferenced);
        assertThrows(IllegalStateException.class, storedObject::markUnreferenced);
    }
}
