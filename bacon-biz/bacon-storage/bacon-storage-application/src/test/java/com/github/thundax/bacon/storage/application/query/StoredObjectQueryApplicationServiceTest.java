package com.github.thundax.bacon.storage.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoredObjectQueryApplicationServiceTest {

    @Mock
    private StoredObjectRepository storedObjectRepository;

    private StoredObjectQueryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new StoredObjectQueryApplicationService(storedObjectRepository);
    }

    @Test
    void shouldRejectQueryForDeletedObject() {
        StoredObject storedObject = StoredObject.newUploadedObject("tenant-a", "LOCAL_FILE", "default",
                "attachment/object-d.bin", "d.bin", "application/octet-stream", 1024L, "/files/d.bin", null);
        storedObject.markDeleting();
        storedObject.markDeleted();
        when(storedObjectRepository.findById(103L)).thenReturn(Optional.of(storedObject));

        assertThrows(NotFoundException.class, () -> service.getObjectById(103L));
    }
}
