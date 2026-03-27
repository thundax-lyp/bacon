package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.storage.application.support.StoredObjectDeletionTransactionService;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoredObjectApplicationServiceTest {

    @Mock
    private StoredObjectRepository storedObjectRepository;
    @Mock
    private StoredObjectReferenceRepository storedObjectReferenceRepository;
    @Mock
    private StoredObjectStorageRepository storedObjectStorageRepository;
    @Mock
    private StorageAuditApplicationService storageAuditApplicationService;
    @Mock
    private StoredObjectDeletionTransactionService storedObjectDeletionTransactionService;
    @Mock
    private StorageUploadLimitValidator storageUploadLimitValidator;

    private StoredObjectApplicationService service;

    @BeforeEach
    void setUp() {
        service = new StoredObjectApplicationService(storedObjectRepository, storedObjectReferenceRepository,
                storedObjectStorageRepository, storageAuditApplicationService, storedObjectDeletionTransactionService,
                storageUploadLimitValidator);
    }

    @Test
    void shouldDeletePhysicalObjectAfterMarkDeleting() {
        StoredObject storedObject = StoredObject.newUploadedObject("tenant-a", "LOCAL_FILE", "default",
                "attachment/object-a.bin", "a.bin", "application/octet-stream", 1024L, "/files/a.bin", null);
        storedObject.markDeleting();
        when(storedObjectDeletionTransactionService.markDeleting(100L)).thenReturn(storedObject);

        service.deleteObject(100L);

        verify(storedObjectStorageRepository).delete(storedObject);
        verify(storedObjectDeletionTransactionService).markDeleted(100L);
    }

    @Test
    void shouldSkipPhysicalDeleteWhenObjectAlreadyDeleted() {
        StoredObject storedObject = StoredObject.newUploadedObject("tenant-a", "LOCAL_FILE", "default",
                "attachment/object-b.bin", "b.bin", "application/octet-stream", 1024L, "/files/b.bin", null);
        storedObject.markDeleting();
        storedObject.markDeleted();
        when(storedObjectDeletionTransactionService.markDeleting(101L)).thenReturn(storedObject);

        service.deleteObject(101L);

        verify(storedObjectStorageRepository, never()).delete(storedObject);
        verify(storedObjectDeletionTransactionService, never()).markDeleted(101L);
    }

    @Test
    void shouldRejectReferenceForDeletingObject() {
        StoredObject storedObject = StoredObject.newUploadedObject("tenant-a", "LOCAL_FILE", "default",
                "attachment/object-c.bin", "c.bin", "application/octet-stream", 1024L, "/files/c.bin", null);
        storedObject.markDeleting();
        when(storedObjectRepository.findById(102L)).thenReturn(Optional.of(storedObject));

        assertThrows(NotFoundException.class,
                () -> service.markObjectReferenced(102L, "GENERIC_ATTACHMENT", "owner-1"));
        verify(storedObjectReferenceRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
