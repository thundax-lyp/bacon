package com.github.thundax.bacon.storage.application.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        StoredObject storedObject = StoredObject.create(
                null,
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-d.bin",
                "d.bin",
                "application/octet-stream",
                1024L,
                "/files/d.bin");
        storedObject.markDeleting();
        storedObject.markDeleted();
        when(storedObjectRepository.findById(StoredObjectId.of(103L))).thenReturn(Optional.of(storedObject));

        assertThrows(NotFoundException.class, () -> service.getObjectById(103L));
    }

    @Test
    void shouldPageObjectsForAdminManagement() {
        StoredObject storedObject = StoredObject.create(
                null,
                StorageType.LOCAL_FILE,
                "default",
                "attachment/object-e.bin",
                "e.bin",
                "application/octet-stream",
                2048L,
                "/files/e.bin");
        when(storedObjectRepository.countObjects(
                        eq("LOCAL_FILE"), eq("ACTIVE"), eq("UNREFERENCED"), eq("e.bin"), eq("attachment")))
                .thenReturn(1L);
        when(storedObjectRepository.pageObjects(
                        eq("LOCAL_FILE"),
                        eq("ACTIVE"),
                        eq("UNREFERENCED"),
                        eq("e.bin"),
                        eq("attachment"),
                        eq(1),
                        eq(200)))
                .thenReturn(List.of(storedObject));

        StoredObjectPageResultDTO result =
                service.pageObjects("LOCAL_FILE", "ACTIVE", "UNREFERENCED", "e.bin", "attachment", 0, 500);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getPageNo());
        assertEquals(200, result.getPageSize());
        assertEquals(1, result.getRecords().size());
        assertEquals("e.bin", result.getRecords().get(0).getOriginalFilename());
    }
}
