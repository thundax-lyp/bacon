package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.application.config.StorageDeletionRetryProperties;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoredObjectDeletionRetryServiceTest {

    @Mock
    private StoredObjectRepository storedObjectRepository;
    @Mock
    private StoredObjectStorageRepository storedObjectStorageRepository;
    @Mock
    private StoredObjectDeletionTransactionService storedObjectDeletionTransactionService;

    private SimpleMeterRegistry meterRegistry;
    private StorageDeletionRetryProperties properties;
    private StoredObjectDeletionRetryService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(meterRegistry);
        properties = new StorageDeletionRetryProperties();
        properties.setEnabled(true);
        properties.setBatchSize(10);
        service = new StoredObjectDeletionRetryService(storedObjectRepository, storedObjectStorageRepository,
                storedObjectDeletionTransactionService, properties);
    }

    @AfterEach
    void tearDown() {
        Metrics.removeRegistry(meterRegistry);
        meterRegistry.close();
    }

    @Test
    void shouldRetryDeletingObjectsAndMarkDeleted() {
        StoredObject storedObject = deletingObject("O100", "attachment/a.bin");
        when(storedObjectRepository.listByObjectStatus(StoredObjectStatus.DELETING, 10))
                .thenReturn(List.of(storedObject));

        int completed = service.retryDeletingObjects();

        assertEquals(1, completed);
        verify(storedObjectStorageRepository).delete(storedObject);
        verify(storedObjectDeletionTransactionService).markDeleted(StoredObjectId.of("O100"));
        assertEquals(1.0d, meterRegistry.get("bacon.storage.deletion.retry.success.total").counter().count());
    }

    @Test
    void shouldKeepDeletingObjectForNextRetryWhenPhysicalDeleteFails() {
        StoredObject storedObject = deletingObject("O101", "attachment/b.bin");
        when(storedObjectRepository.listByObjectStatus(StoredObjectStatus.DELETING, 10))
                .thenReturn(List.of(storedObject));
        doThrow(new IllegalStateException("delete-fail")).when(storedObjectStorageRepository).delete(storedObject);

        int completed = service.retryDeletingObjects();

        assertEquals(0, completed);
        verify(storedObjectDeletionTransactionService, never()).markDeleted(StoredObjectId.of("O101"));
        assertEquals(1.0d, meterRegistry.get("bacon.storage.deletion.retry.fail.total").counter().count());
    }

    @Test
    void shouldSkipRetryWhenDisabled() {
        properties.setEnabled(false);

        int completed = service.retryDeletingObjects();

        assertEquals(0, completed);
        verify(storedObjectRepository, never()).listByObjectStatus(eq(StoredObjectStatus.DELETING), anyInt());
    }

    private StoredObject deletingObject(String id, String objectKey) {
        StoredObject storedObject = StoredObject.newUploadedObject(TenantId.of("tenant-a"), StorageType.LOCAL_FILE, "default", objectKey,
                "test.bin", "application/octet-stream", 1024L, "/files/test.bin", null);
        storedObject.markDeleting();
        return new StoredObject(StoredObjectId.of(id), storedObject.getTenantId(), storedObject.getStorageType(), storedObject.getBucketName(),
                storedObject.getObjectKey(), storedObject.getOriginalFilename(), storedObject.getContentType(),
                storedObject.getSize(), storedObject.getAccessEndpoint(), storedObject.getObjectStatus(),
                storedObject.getReferenceStatus(), storedObject.getCreatedBy(), storedObject.getCreatedAt(),
                storedObject.getUpdatedBy(), storedObject.getUpdatedAt());
    }
}
