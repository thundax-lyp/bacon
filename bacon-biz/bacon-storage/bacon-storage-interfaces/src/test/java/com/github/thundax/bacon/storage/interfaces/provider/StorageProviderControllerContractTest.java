package com.github.thundax.bacon.storage.interfaces.provider;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.storage.application.command.StoredObjectCommandApplicationService;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class StorageProviderControllerContractTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "storage-token";

    @Mock
    private StoredObjectCommandApplicationService storedObjectCommandApplicationService;

    @Mock
    private StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    @Test
    void shouldExposeUploadAndMultipartCommandContracts() throws Exception {
        when(storedObjectCommandApplicationService.uploadObject(argThat(command -> command != null
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.category().equals("attachment")
                        && command.originalFilename().equals("a.txt")
                        && command.contentType().equals("text/plain")
                        && command.size().equals(3L))))
                .thenReturn(objectDto("storage-1"));
        when(storedObjectCommandApplicationService.initMultipartUpload(argThat(command -> command != null
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.ownerId().equals("owner-1")
                        && command.totalSize().equals(1024L)
                        && command.partSize().equals(512L))))
                .thenReturn(sessionDto());
        when(storedObjectCommandApplicationService.uploadMultipartPart(argThat(command -> command != null
                        && command.uploadId().equals("upload-1")
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.ownerId().equals("owner-1")
                        && command.partNumber().equals(1)
                        && command.size().equals(3L))))
                .thenReturn(new MultipartUploadPartDTO("upload-1", 1, "etag-1"));
        when(storedObjectCommandApplicationService.completeMultipartUpload(argThat(command -> command != null
                        && command.uploadId().equals("upload-1")
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.ownerId().equals("owner-1"))))
                .thenReturn(objectDto("storage-2"));
        MockMvc mockMvc = newMockMvc();
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/providers/storage/commands/upload-object")
                        .file(file)
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("category", "attachment")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storedObjectNo").value("storage-1"))
                .andExpect(jsonPath("$.code").doesNotExist());
        mockMvc.perform(post("/providers/storage/commands/init-multipart-upload")
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .param("category", "attachment")
                        .param("originalFilename", "a.txt")
                        .param("contentType", "text/plain")
                        .param("totalSize", "1024")
                        .param("partSize", "512")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("upload-1"));
        mockMvc.perform(multipart("/providers/storage/commands/upload-multipart-part")
                        .file(file)
                        .param("uploadId", "upload-1")
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .param("partNumber", "1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etag").value("etag-1"));
        mockMvc.perform(post("/providers/storage/commands/complete-multipart-upload")
                        .param("uploadId", "upload-1")
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storedObjectNo").value("storage-2"));
    }

    @Test
    void shouldExposeReadReferenceAndDeleteProviderContracts() throws Exception {
        when(storedObjectQueryApplicationService.getObjectByNo(argThat(query -> query != null
                        && query.storedObjectNo().value().equals("storage-1"))))
                .thenReturn(objectDto("storage-1"));
        MockMvc mockMvc = newMockMvc();

        mockMvc.perform(get("/providers/storage/queries/object")
                        .param("storedObjectNo", "storage-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storedObjectNo").value("storage-1"))
                .andExpect(jsonPath("$.code").doesNotExist());
        mockMvc.perform(MockMvcRequestBuilders.delete("/providers/storage/commands/abort-multipart-upload")
                        .param("uploadId", "upload-1")
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());
        mockMvc.perform(post("/providers/storage/commands/mark-object-referenced")
                        .param("storedObjectNo", "storage-1")
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.delete("/providers/storage/commands/clear-object-reference")
                        .param("storedObjectNo", "storage-1")
                        .param("ownerType", "GENERIC_ATTACHMENT")
                        .param("ownerId", "owner-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.delete("/providers/storage/commands/delete-object")
                        .param("storedObjectNo", "storage-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());

        verify(storedObjectCommandApplicationService)
                .abortMultipartUpload(argThat(command -> command.uploadId().equals("upload-1")));
        verify(storedObjectCommandApplicationService)
                .markObjectReferenced(argThat(command -> command.storedObjectNo().value().equals("storage-1")
                        && command.ownerType().equals("GENERIC_ATTACHMENT")
                        && command.ownerId().equals("owner-1")));
        verify(storedObjectCommandApplicationService)
                .clearObjectReference(argThat(command -> command.storedObjectNo().value().equals("storage-1")));
        verify(storedObjectCommandApplicationService)
                .deleteObject(argThat(command -> command.storedObjectNo().value().equals("storage-1")));
    }

    private MockMvc newMockMvc() {
        StorageProviderController controller =
                new StorageProviderController(storedObjectCommandApplicationService, storedObjectQueryApplicationService);
        return MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/storage/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }

    private StoredObjectDTO objectDto(String storedObjectNo) {
        return new StoredObjectDTO(
                storedObjectNo,
                "LOCAL_FILE",
                "default",
                "attachment/a.txt",
                "a.txt",
                "text/plain",
                3L,
                "/files/attachment/a.txt",
                "ACTIVE",
                "UNREFERENCED",
                Instant.parse("2026-03-27T10:00:00Z"));
    }

    private MultipartUploadSessionDTO sessionDto() {
        return new MultipartUploadSessionDTO(
                "upload-1", "GENERIC_ATTACHMENT", "owner-1", "attachment", "a.txt", "text/plain", 1024L, 512L, 0,
                "INITIATED");
    }
}
