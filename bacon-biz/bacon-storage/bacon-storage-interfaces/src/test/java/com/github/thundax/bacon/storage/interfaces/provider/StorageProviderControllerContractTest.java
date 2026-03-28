package com.github.thundax.bacon.storage.interfaces.provider;

import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.application.command.MultipartUploadApplicationService;
import com.github.thundax.bacon.storage.application.command.StoredObjectApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StorageProviderControllerContractTest {

    private MockMvc mockMvc;
    private MultipartUploadApplicationService multipartUploadApplicationService;
    private StoredObjectApplicationService storedObjectApplicationService;

    @BeforeEach
    void setUp() {
        storedObjectApplicationService = Mockito.mock(StoredObjectApplicationService.class);
        multipartUploadApplicationService = Mockito.mock(MultipartUploadApplicationService.class);
        StoredObjectQueryApplicationService storedObjectQueryApplicationService = Mockito.mock(StoredObjectQueryApplicationService.class);
        StorageProviderController controller = new StorageProviderController(storedObjectApplicationService,
                multipartUploadApplicationService, storedObjectQueryApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldExposeUploadProviderPath() throws Exception {
        StoredObjectDTO dto = new StoredObjectDTO(1L, "LOCAL_FILE", "default", "attachment/a.txt", "a.txt",
                "text/plain", 3L, "/files/attachment/a.txt", "ACTIVE", "UNREFERENCED",
                Instant.parse("2026-03-27T10:00:00Z"));
        when(storedObjectApplicationService.uploadObject(any())).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/providers/storage/objects/upload")
                        .file(file)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("tenantId", "tenant-a")
                        .param("category", "attachment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMultipartInitPath() throws Exception {
        when(multipartUploadApplicationService.initMultipartUpload(any())).thenReturn(new MultipartUploadSessionDTO(
                "upload-1", "GENERIC_ATTACHMENT", "owner-1", "tenant-a", "attachment", "a.txt",
                "text/plain", 1024L, 8L * 1024 * 1024, 0, "INITIATED"));

        mockMvc.perform(post("/providers/storage/objects/multipart/init")
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .param("tenantId", "tenant-a")
                        .param("category", "attachment")
                        .param("originalFilename", "a.txt")
                        .param("contentType", "text/plain")
                        .param("totalSize", "1024")
                        .param("partSize", String.valueOf(8L * 1024 * 1024)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("upload-1"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMultipartPartUploadPath() throws Exception {
        when(multipartUploadApplicationService.uploadMultipartPart(any()))
                .thenReturn(new MultipartUploadPartDTO("upload-1", 1, "etag-1"));
        MockMultipartFile file = new MockMultipartFile("file", "part-1.bin",
                "application/octet-stream", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/providers/storage/objects/multipart/{uploadId}/parts", "upload-1")
                        .file(file)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .param("tenantId", "tenant-a")
                        .param("partNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("upload-1"))
                .andExpect(jsonPath("$.partNumber").value(1))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMultipartCompletePath() throws Exception {
        when(multipartUploadApplicationService.completeMultipartUpload(any())).thenReturn(new StoredObjectDTO(
                2L, "OSS", "bucket", "attachment/a.txt", "a.txt", "text/plain", 1024L,
                "http://test/attachment/a.txt", "ACTIVE", "UNREFERENCED", Instant.parse("2026-03-27T10:00:00Z")));

        mockMvc.perform(post("/providers/storage/objects/multipart/{uploadId}/complete", "upload-1")
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .param("tenantId", "tenant-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMultipartAbortPath() throws Exception {
        doNothing().when(multipartUploadApplicationService).abortMultipartUpload(any());

        mockMvc.perform(MockMvcRequestBuilders.delete("/providers/storage/objects/multipart/{uploadId}", "upload-1")
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .param("tenantId", "tenant-a"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExposeGetObjectPath() throws Exception {
        StoredObjectQueryApplicationService storedObjectQueryApplicationService = Mockito.mock(StoredObjectQueryApplicationService.class);
        StorageProviderController controller = new StorageProviderController(storedObjectApplicationService,
                multipartUploadApplicationService, storedObjectQueryApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(storedObjectQueryApplicationService.getObjectById(100L)).thenReturn(new StoredObjectDTO(
                100L, "LOCAL_FILE", "default", "attachment/a.txt", "a.txt", "text/plain", 3L,
                "/files/attachment/a.txt", "ACTIVE", "UNREFERENCED", Instant.parse("2026-03-27T10:00:00Z")));

        mockMvc.perform(get("/providers/storage/objects/{objectId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposePageObjectsPath() throws Exception {
        StoredObjectQueryApplicationService storedObjectQueryApplicationService = Mockito.mock(StoredObjectQueryApplicationService.class);
        StorageProviderController controller = new StorageProviderController(storedObjectApplicationService,
                multipartUploadApplicationService, storedObjectQueryApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(storedObjectQueryApplicationService.pageObjects(any())).thenReturn(new StoredObjectPageResultDTO(
                java.util.List.of(new StoredObjectDTO(101L, "LOCAL_FILE", "default", "attachment/e.txt", "e.txt",
                        "text/plain", 5L, "/files/attachment/e.txt", "ACTIVE", "UNREFERENCED",
                        Instant.parse("2026-03-27T10:00:00Z"))), 1L, 1, 20));

        mockMvc.perform(get("/providers/storage/objects")
                        .param("tenantId", "tenant-a")
                        .param("storageType", "LOCAL_FILE")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.records[0].id").value(101))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMarkReferencePath() throws Exception {
        doNothing().when(storedObjectApplicationService).markObjectReferenced(100L, "GENERIC_ATTACHMENT", "owner-1");

        mockMvc.perform(post("/providers/storage/objects/{objectId}/references", 100L)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExposeClearReferencePath() throws Exception {
        doNothing().when(storedObjectApplicationService).clearObjectReference(100L, "GENERIC_ATTACHMENT", "owner-1");

        mockMvc.perform(MockMvcRequestBuilders.delete("/providers/storage/objects/{objectId}/references", 100L)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExposeDeleteObjectPath() throws Exception {
        doNothing().when(storedObjectApplicationService).deleteObject(100L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/providers/storage/objects/{objectId}", 100L))
                .andExpect(status().isOk());
    }
}
