package com.github.thundax.bacon.storage.interfaces.provider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.application.command.StoredObjectCommandApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class StorageProviderControllerContractTest {

    private static final String PROVIDER_TOKEN = "storage-token";

    private MockMvc mockMvc;
    private StoredObjectCommandApplicationService storedObjectCommandApplicationService;

    @BeforeEach
    void setUp() {
        storedObjectCommandApplicationService = Mockito.mock(StoredObjectCommandApplicationService.class);
        StoredObjectQueryApplicationService storedObjectQueryApplicationService =
                Mockito.mock(StoredObjectQueryApplicationService.class);
        StorageProviderController controller =
                new StorageProviderController(storedObjectCommandApplicationService, storedObjectQueryApplicationService);
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/storage/**"));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new InternalApiGuardInterceptor(guardProperties))
                .build();
    }

    @Test
    void shouldExposeUploadProviderPath() throws Exception {
        when(storedObjectCommandApplicationService.uploadObject(any())).thenReturn(new StoredObjectDTO(
                "storage-20260327100000-000001",
                "LOCAL_FILE",
                "default",
                "attachment/a.txt",
                "a.txt",
                "text/plain",
                3L,
                "/files/attachment/a.txt",
                "ACTIVE",
                "UNREFERENCED",
                Instant.parse("2026-03-27T10:00:00Z")));

        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/providers/storage/objects/upload")
                        .file(file)
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("category", "attachment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storedObjectNo").value("storage-20260327100000-000001"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMultipartInitPath() throws Exception {
        when(storedObjectCommandApplicationService.initMultipartUpload(any()))
                .thenReturn(new MultipartUploadSessionDTO(
                        "storage20260327100000-001001",
                        "GENERIC_ATTACHMENT",
                        "owner-1",
                        "attachment",
                        "a.txt",
                        "text/plain",
                        1024L,
                        8L * 1024 * 1024,
                        0,
                        "INITIATED"));

        mockMvc.perform(post("/providers/storage/objects/multipart/init")
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .param("category", "attachment")
                        .param("originalFilename", "a.txt")
                        .param("contentType", "text/plain")
                        .param("totalSize", "1024")
                        .param("partSize", String.valueOf(8L * 1024 * 1024)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("storage20260327100000-001001"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMultipartPartUploadPath() throws Exception {
        when(storedObjectCommandApplicationService.uploadMultipartPart(any()))
                .thenReturn(new MultipartUploadPartDTO("1", 1, "etag-1"));
        MockMultipartFile file =
                new MockMultipartFile("file", "part-1.bin", "application/octet-stream", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/providers/storage/objects/multipart/{uploadId}/parts", "1")
                        .file(file)
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .param("partNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("1"))
                .andExpect(jsonPath("$.partNumber").value(1))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMultipartCompletePath() throws Exception {
        when(storedObjectCommandApplicationService.completeMultipartUpload(any()))
                .thenReturn(new StoredObjectDTO(
                        "storage-20260327100000-000002",
                        "OSS",
                        "bucket",
                        "attachment/a.txt",
                        "a.txt",
                        "text/plain",
                        1024L,
                        "http://test/attachment/a.txt",
                        "ACTIVE",
                        "UNREFERENCED",
                        Instant.parse("2026-03-27T10:00:00Z")));

        mockMvc.perform(post("/providers/storage/objects/multipart/{uploadId}/complete", "1")
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storedObjectNo").value("storage-20260327100000-000002"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMultipartAbortPath() throws Exception {
        doNothing().when(storedObjectCommandApplicationService).abortMultipartUpload(any());

        mockMvc.perform(MockMvcRequestBuilders.delete("/providers/storage/objects/multipart/{uploadId}", "1")
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExposeGetObjectPath() throws Exception {
        StoredObjectQueryApplicationService storedObjectQueryApplicationService =
                Mockito.mock(StoredObjectQueryApplicationService.class);
        StorageProviderController controller =
                new StorageProviderController(storedObjectCommandApplicationService, storedObjectQueryApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
        when(storedObjectQueryApplicationService.getObjectByNo(any()))
                .thenReturn(new StoredObjectDTO(
                        "storage-20260327100000-000100",
                        "LOCAL_FILE",
                        "default",
                        "attachment/a.txt",
                        "a.txt",
                        "text/plain",
                        3L,
                        "/files/attachment/a.txt",
                        "ACTIVE",
                        "UNREFERENCED",
                        Instant.parse("2026-03-27T10:00:00Z")));

        mockMvc.perform(get("/providers/storage/objects/{storedObjectNo}", "storage-20260327100000-000100")
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storedObjectNo").value("storage-20260327100000-000100"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposePageObjectsPath() throws Exception {
        StoredObjectQueryApplicationService storedObjectQueryApplicationService =
                Mockito.mock(StoredObjectQueryApplicationService.class);
        StorageProviderController controller =
                new StorageProviderController(storedObjectCommandApplicationService, storedObjectQueryApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
        when(storedObjectQueryApplicationService.page(any()))
                .thenReturn(new PageResult<>(
                        java.util.List.of(new StoredObjectDTO(
                                "storage-20260327100000-000101",
                                "LOCAL_FILE",
                                "default",
                                "attachment/e.txt",
                                "e.txt",
                                "text/plain",
                                5L,
                                "/files/attachment/e.txt",
                                "ACTIVE",
                        "UNREFERENCED",
                        Instant.parse("2026-03-27T10:00:00Z"))),
                        1L,
                        1,
                        20));

        mockMvc.perform(get("/providers/storage/objects")
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN)
                        .param("tenantId", "1")
                        .param("storageType", "LOCAL_FILE")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.records[0].storedObjectNo").value("storage-20260327100000-000101"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeMarkReferencePath() throws Exception {
        doNothing().when(storedObjectCommandApplicationService).markObjectReferenced(any());

        mockMvc.perform(post("/providers/storage/objects/{storedObjectNo}/references", "storage-20260327100000-000100")
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExposeClearReferencePath() throws Exception {
        doNothing().when(storedObjectCommandApplicationService).clearObjectReference(any());

        mockMvc.perform(MockMvcRequestBuilders.delete(
                                "/providers/storage/objects/{storedObjectNo}/references", "storage-20260327100000-000100")
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExposeDeleteObjectPath() throws Exception {
        doNothing().when(storedObjectCommandApplicationService).deleteObject(any());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        "/providers/storage/objects/{storedObjectNo}", "storage-20260327100000-000100")
                        .header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectProviderRequestWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/providers/storage/objects/{storedObjectNo}", "storage-20260327100000-000100"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProviderRequestWhenTokenInvalid() throws Exception {
        mockMvc.perform(get("/providers/storage/objects/{storedObjectNo}", "storage-20260327100000-000100")
                        .header("X-Bacon-Provider-Token", "wrong-token"))
                .andExpect(status().isForbidden());
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/storage/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }
}
