package com.github.thundax.bacon.storage.application.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.application.support.StoredObjectDeletionTransactionService;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
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
class StoredObjectCommandApplicationServiceTest {

    @Mock
    private StoredObjectRepository storedObjectRepository;

    @Mock
    private StoredObjectReferenceRepository storedObjectReferenceRepository;

    @Mock
    private StoredObjectStorageRepository storedObjectStorageRepository;

    @Mock
    private MultipartUploadSessionRepository multipartUploadSessionRepository;

    @Mock
    private MultipartUploadPartRepository multipartUploadPartRepository;

    @Mock
    private StorageAuditApplicationService storageAuditApplicationService;

    @Mock
    private StoredObjectDeletionTransactionService storedObjectDeletionTransactionService;

    @Mock
    private StorageUploadLimitValidator storageUploadLimitValidator;

    @Mock
    private IdGenerator idGenerator;

    private StoredObjectCommandApplicationService service;

    @BeforeEach
    void setUp() {
        service = new StoredObjectCommandApplicationService(
                storedObjectRepository,
                storedObjectReferenceRepository,
                storedObjectStorageRepository,
                multipartUploadSessionRepository,
                multipartUploadPartRepository,
                storageAuditApplicationService,
                storedObjectDeletionTransactionService,
                storageUploadLimitValidator,
                idGenerator);
    }

    @Test
    void shouldDeletePhysicalObjectAfterMarkDeleting() {
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(100L),
                StoredObjectNo.of("storage-20260327100000-000100"),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-a.bin",
                "a.bin",
                "application/octet-stream",
                1024L,
                "/files/a.bin");
        storedObject.markDeleting();
        when(storedObjectRepository.findByNo(StoredObjectNo.of("storage-20260327100000-000100")))
                .thenReturn(Optional.of(storedObject));
        when(storedObjectDeletionTransactionService.markDeleting(StoredObjectId.of(100L)))
                .thenReturn(storedObject);

        service.deleteObject(new StoredObjectDeleteCommand(StoredObjectNo.of("storage-20260327100000-000100")));

        verify(storedObjectStorageRepository).delete(storedObject);
        verify(storedObjectDeletionTransactionService).markDeleted(StoredObjectId.of(100L));
    }

    @Test
    void shouldSkipPhysicalDeleteWhenObjectAlreadyDeleted() {
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(101L),
                StoredObjectNo.of("storage-20260327100000-000101"),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-b.bin",
                "b.bin",
                "application/octet-stream",
                1024L,
                "/files/b.bin");
        storedObject.markDeleting();
        storedObject.markDeleted();
        when(storedObjectRepository.findByNo(StoredObjectNo.of("storage-20260327100000-000101")))
                .thenReturn(Optional.of(storedObject));
        when(storedObjectDeletionTransactionService.markDeleting(StoredObjectId.of(101L)))
                .thenReturn(storedObject);

        service.deleteObject(new StoredObjectDeleteCommand(StoredObjectNo.of("storage-20260327100000-000101")));

        verify(storedObjectStorageRepository, never()).delete(storedObject);
        verify(storedObjectDeletionTransactionService, never()).markDeleted(StoredObjectId.of(101L));
    }

    @Test
    void shouldRejectReferenceForDeletingObject() {
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(102L),
                StoredObjectNo.of("storage-20260327100000-000102"),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-c.bin",
                "c.bin",
                "application/octet-stream",
                1024L,
                "/files/c.bin");
        storedObject.markDeleting();
        when(storedObjectRepository.findByNo(StoredObjectNo.of("storage-20260327100000-000102")))
                .thenReturn(Optional.of(storedObject));

        assertThrows(
                NotFoundException.class,
                () -> service.markObjectReferenced(new StoredObjectReferenceCommand(
                        StoredObjectNo.of("storage-20260327100000-000102"), "GENERIC_ATTACHMENT", "owner-1")));
        verify(storedObjectReferenceRepository, never()).insert(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldSkipDuplicateReferenceAuditWhenReferenceAlreadyExists() {
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(103L),
                StoredObjectNo.of("storage-20260327100000-000103"),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-d.bin",
                "d.bin",
                "application/octet-stream",
                1024L,
                "/files/d.bin");
        when(storedObjectRepository.findByNo(StoredObjectNo.of("storage-20260327100000-000103")))
                .thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.insert(any())).thenReturn(false);
        when(storedObjectReferenceRepository.exists(StoredObjectId.of(103L)))
                .thenReturn(true);
        when(storedObjectRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.markObjectReferenced(new StoredObjectReferenceCommand(
                StoredObjectNo.of("storage-20260327100000-000103"), "GENERIC_ATTACHMENT", "owner-1"));

        verify(storedObjectReferenceRepository).insert(any());
        verify(storedObjectRepository).update(any());
        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldSkipMissingReferenceClearAudit() {
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(104L),
                StoredObjectNo.of("storage-20260327100000-000104"),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-e.bin",
                "e.bin",
                "application/octet-stream",
                1024L,
                "/files/e.bin");
        storedObject.markReferenced();
        when(storedObjectRepository.findByNo(StoredObjectNo.of("storage-20260327100000-000104")))
                .thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.delete(
                        StoredObjectId.of(104L), "GENERIC_ATTACHMENT", "owner-2"))
                .thenReturn(false);
        when(storedObjectReferenceRepository.exists(StoredObjectId.of(104L)))
                .thenReturn(true);

        service.clearObjectReference(new StoredObjectReferenceCommand(
                StoredObjectNo.of("storage-20260327100000-000104"), "GENERIC_ATTACHMENT", "owner-2"));

        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldReconcileReferenceStatusWhenReferenceInsertHitsDuplicate() {
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(105L),
                StoredObjectNo.of("storage-20260327100000-000105"),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-f.bin",
                "f.bin",
                "application/octet-stream",
                1024L,
                "/files/f.bin");
        when(storedObjectRepository.findByNo(StoredObjectNo.of("storage-20260327100000-000105")))
                .thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.insert(any())).thenReturn(false);
        when(storedObjectReferenceRepository.exists(StoredObjectId.of(105L)))
                .thenReturn(true);
        when(storedObjectRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.markObjectReferenced(new StoredObjectReferenceCommand(
                StoredObjectNo.of("storage-20260327100000-000105"), "GENERIC_ATTACHMENT", "owner-3"));

        verify(storedObjectRepository).update(any());
        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldReconcileReferenceStatusWhenReferenceDeleteAlreadyConsumed() {
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(106L),
                StoredObjectNo.of("storage-20260327100000-000106"),
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-g.bin",
                "g.bin",
                "application/octet-stream",
                1024L,
                "/files/g.bin");
        storedObject.markReferenced();
        when(storedObjectRepository.findByNo(StoredObjectNo.of("storage-20260327100000-000106")))
                .thenReturn(Optional.of(storedObject));
        when(storedObjectReferenceRepository.delete(
                        StoredObjectId.of(106L), "GENERIC_ATTACHMENT", "owner-4"))
                .thenReturn(false);
        when(storedObjectReferenceRepository.exists(StoredObjectId.of(106L)))
                .thenReturn(false);
        when(storedObjectRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.clearObjectReference(new StoredObjectReferenceCommand(
                StoredObjectNo.of("storage-20260327100000-000106"), "GENERIC_ATTACHMENT", "owner-4"));

        verify(storedObjectRepository).update(any());
        verify(storageAuditApplicationService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }
}
