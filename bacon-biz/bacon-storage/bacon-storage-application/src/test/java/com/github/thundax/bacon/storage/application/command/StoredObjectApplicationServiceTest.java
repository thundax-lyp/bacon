package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.application.support.StoredObjectDeletionTransactionService;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
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
import static org.mockito.ArgumentMatchers.any;
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
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of("tenant-a"), StorageType.LOCAL_FILE, "default",
                "attachment/object-a.bin", "a.bin", "application/octet-stream", 1024L, "/files/a.bin", null);
        storedObject.markDeleting();
        when(storedObjectDeletionTransactionService.markDeleting(StoredObjectId.of("O100"))).thenReturn(storedObject);

        service.deleteObject("O100");

        verify(storedObjectStorageRepository).delete(storedObject);
        verify(storedObjectDeletionTransactionService).markDeleted(StoredObjectId.of("O100"));
    }

    @Test
    void shouldSkipPhysicalDeleteWhenObjectAlreadyDeleted() {
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of("tenant-a"), StorageType.LOCAL_FILE, "default",
                "attachment/object-b.bin", "b.bin", "application/octet-stream", 1024L, "/files/b.bin", null);
        storedObject.markDeleting();
        storedObject.markDeleted();
        when(storedObjectDeletionTransactionService.markDeleting(StoredObjectId.of("O101"))).thenReturn(storedObject);

        service.deleteObject("O101");

        verify(storedObjectStorageRepository, never()).delete(storedObject);
        verify(storedObjectDeletionTransactionService, never()).markDeleted(StoredObjectId.of("O101"));
    }

    @Test
    void shouldRejectReferenceForDeletingObject() {
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of("tenant-a"), StorageType.LOCAL_FILE, "default",
                "attachment/object-c.bin", "c.bin", "application/octet-stream", 1024L, "/files/c.bin", null);
        storedObject.markDeleting();
        when(storedObjectRepository.findById(StoredObjectId.of("O102"))).thenReturn(Optional.of(storedObject));

        assertThrows(NotFoundException.class,
                () -> service.markObjectReferenced("O102", "GENERIC_ATTACHMENT", "owner-1"));
        verify(storedObjectReferenceRepository, never()).saveIfAbsent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldSkipDuplicateReferenceAuditWhenReferenceAlreadyExists() {
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of("tenant-a"), StorageType.LOCAL_FILE, "default",
                "attachment/object-d.bin", "d.bin", "application/octet-stream", 1024L, "/files/d.bin", null);
        when(storedObjectRepository.findById(StoredObjectId.of("O103"))).thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.saveIfAbsent(any())).thenReturn(false);
        when(storedObjectReferenceRepository.existsByObjectId(StoredObjectId.of("O103"))).thenReturn(true);
        when(storedObjectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.markObjectReferenced("O103", "GENERIC_ATTACHMENT", "owner-1");

        verify(storedObjectReferenceRepository).saveIfAbsent(any());
        verify(storedObjectRepository).save(any());
        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldSkipMissingReferenceClearAudit() {
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of("tenant-a"), StorageType.LOCAL_FILE, "default",
                "attachment/object-e.bin", "e.bin", "application/octet-stream", 1024L, "/files/e.bin", null);
        storedObject.markReferenced();
        when(storedObjectRepository.findById(StoredObjectId.of("O104"))).thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.deleteByObjectIdAndOwner(StoredObjectId.of("O104"), "GENERIC_ATTACHMENT", "owner-2"))
                .thenReturn(false);
        when(storedObjectReferenceRepository.existsByObjectId(StoredObjectId.of("O104"))).thenReturn(true);

        service.clearObjectReference("O104", "GENERIC_ATTACHMENT", "owner-2");

        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldReconcileReferenceStatusWhenReferenceInsertHitsDuplicate() {
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of("tenant-a"), StorageType.LOCAL_FILE, "default",
                "attachment/object-f.bin", "f.bin", "application/octet-stream", 1024L, "/files/f.bin", null);
        when(storedObjectRepository.findById(StoredObjectId.of("O105"))).thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.saveIfAbsent(any())).thenReturn(false);
        when(storedObjectReferenceRepository.existsByObjectId(StoredObjectId.of("O105"))).thenReturn(true);
        when(storedObjectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.markObjectReferenced("O105", "GENERIC_ATTACHMENT", "owner-3");

        verify(storedObjectRepository).save(any());
        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldReconcileReferenceStatusWhenReferenceDeleteAlreadyConsumed() {
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of("tenant-a"), StorageType.LOCAL_FILE, "default",
                "attachment/object-g.bin", "g.bin", "application/octet-stream", 1024L, "/files/g.bin", null);
        storedObject.markReferenced();
        when(storedObjectRepository.findById(StoredObjectId.of("O106"))).thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.deleteByObjectIdAndOwner(StoredObjectId.of("O106"), "GENERIC_ATTACHMENT", "owner-4"))
                .thenReturn(false);
        when(storedObjectReferenceRepository.existsByObjectId(StoredObjectId.of("O106"))).thenReturn(false);
        when(storedObjectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.clearObjectReference("O106", "GENERIC_ATTACHMENT", "owner-4");

        verify(storedObjectRepository).save(any());
        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }
}
