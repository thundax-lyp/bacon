package com.github.thundax.bacon.storage.application.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.application.support.StoredObjectDeletionTransactionService;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        service = new StoredObjectApplicationService(
                storedObjectRepository,
                storedObjectReferenceRepository,
                storedObjectStorageRepository,
                storageAuditApplicationService,
                storedObjectDeletionTransactionService,
                storageUploadLimitValidator);
    }

    @Test
    void shouldDeletePhysicalObjectAfterMarkDeleting() {
        StoredObject storedObject = StoredObject.newUploadedObject(
                TenantId.of(1L),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-a.bin",
                "a.bin",
                "application/octet-stream",
                1024L,
                "/files/a.bin",
                null);
        storedObject.markDeleting();
        when(storedObjectDeletionTransactionService.markDeleting(StoredObjectId.of(100L)))
                .thenReturn(storedObject);

        service.deleteObject(100L);

        verify(storedObjectStorageRepository).delete(storedObject);
        verify(storedObjectDeletionTransactionService).markDeleted(StoredObjectId.of(100L));
    }

    @Test
    void shouldSkipPhysicalDeleteWhenObjectAlreadyDeleted() {
        StoredObject storedObject = StoredObject.newUploadedObject(
                TenantId.of(1L),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-b.bin",
                "b.bin",
                "application/octet-stream",
                1024L,
                "/files/b.bin",
                null);
        storedObject.markDeleting();
        storedObject.markDeleted();
        when(storedObjectDeletionTransactionService.markDeleting(StoredObjectId.of(101L)))
                .thenReturn(storedObject);

        service.deleteObject(101L);

        verify(storedObjectStorageRepository, never()).delete(storedObject);
        verify(storedObjectDeletionTransactionService, never()).markDeleted(StoredObjectId.of(101L));
    }

    @Test
    void shouldRejectReferenceForDeletingObject() {
        StoredObject storedObject = StoredObject.newUploadedObject(
                TenantId.of(1L),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-c.bin",
                "c.bin",
                "application/octet-stream",
                1024L,
                "/files/c.bin",
                null);
        storedObject.markDeleting();
        when(storedObjectRepository.findById(StoredObjectId.of(102L))).thenReturn(Optional.of(storedObject));

        assertThrows(
                NotFoundException.class, () -> service.markObjectReferenced(102L, "GENERIC_ATTACHMENT", "owner-1"));
        verify(storedObjectReferenceRepository, never()).saveIfAbsent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldSkipDuplicateReferenceAuditWhenReferenceAlreadyExists() {
        StoredObject storedObject = StoredObject.newUploadedObject(
                TenantId.of(1L),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-d.bin",
                "d.bin",
                "application/octet-stream",
                1024L,
                "/files/d.bin",
                null);
        when(storedObjectRepository.findById(StoredObjectId.of(103L))).thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.saveIfAbsent(any())).thenReturn(false);
        when(storedObjectReferenceRepository.existsByObjectId(StoredObjectId.of(103L)))
                .thenReturn(true);
        when(storedObjectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.markObjectReferenced(103L, "GENERIC_ATTACHMENT", "owner-1");

        verify(storedObjectReferenceRepository).saveIfAbsent(any());
        verify(storedObjectRepository).save(any());
        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldSkipMissingReferenceClearAudit() {
        StoredObject storedObject = StoredObject.newUploadedObject(
                TenantId.of(1L),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-e.bin",
                "e.bin",
                "application/octet-stream",
                1024L,
                "/files/e.bin",
                null);
        storedObject.markReferenced();
        when(storedObjectRepository.findById(StoredObjectId.of(104L))).thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.deleteByObjectIdAndOwner(
                        StoredObjectId.of(104L), "GENERIC_ATTACHMENT", "owner-2"))
                .thenReturn(false);
        when(storedObjectReferenceRepository.existsByObjectId(StoredObjectId.of(104L)))
                .thenReturn(true);

        service.clearObjectReference(104L, "GENERIC_ATTACHMENT", "owner-2");

        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldReconcileReferenceStatusWhenReferenceInsertHitsDuplicate() {
        StoredObject storedObject = StoredObject.newUploadedObject(
                TenantId.of(1L),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-f.bin",
                "f.bin",
                "application/octet-stream",
                1024L,
                "/files/f.bin",
                null);
        when(storedObjectRepository.findById(StoredObjectId.of(105L))).thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.saveIfAbsent(any())).thenReturn(false);
        when(storedObjectReferenceRepository.existsByObjectId(StoredObjectId.of(105L)))
                .thenReturn(true);
        when(storedObjectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.markObjectReferenced(105L, "GENERIC_ATTACHMENT", "owner-3");

        verify(storedObjectRepository).save(any());
        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldReconcileReferenceStatusWhenReferenceDeleteAlreadyConsumed() {
        StoredObject storedObject = StoredObject.newUploadedObject(
                TenantId.of(1L),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-g.bin",
                "g.bin",
                "application/octet-stream",
                1024L,
                "/files/g.bin",
                null);
        storedObject.markReferenced();
        when(storedObjectRepository.findById(StoredObjectId.of(106L))).thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.deleteByObjectIdAndOwner(
                        StoredObjectId.of(106L), "GENERIC_ATTACHMENT", "owner-4"))
                .thenReturn(false);
        when(storedObjectReferenceRepository.existsByObjectId(StoredObjectId.of(106L)))
                .thenReturn(false);
        when(storedObjectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.clearObjectReference(106L, "GENERIC_ATTACHMENT", "owner-4");

        verify(storedObjectRepository).save(any());
        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }
}
