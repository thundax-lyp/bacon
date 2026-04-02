package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.application.command.UserApplicationService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerContractTest {

    private static final TenantId TENANT_ID = TenantId.of("tenant-demo");

    private UserApplicationService userApplicationService;
    private TenantRequestResolver tenantRequestResolver;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userApplicationService = mock(UserApplicationService.class);
        tenantRequestResolver = mock(TenantRequestResolver.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userApplicationService, tenantRequestResolver)).build();
    }

    @Test
    void shouldUploadAvatarThroughMultipartPutEndpoint() throws Exception {
        when(tenantRequestResolver.resolveTenantId("tenant-demo")).thenReturn(TENANT_ID);
        when(userApplicationService.updateAvatar(eq(TENANT_ID), eq("U101"), eq("avatar.png"), eq("image/png"), eq(4L),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new UserDTO("U101", "tenant-demo", "alice", "Alice", "O9001", "13800000001", "D11",
                        "https://cdn.example.com/avatar/9001.png", "ENABLED"));

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3, 4});

        mockMvc.perform(multipart("/upms/users/{userId}/avatar", "U101")
                        .file(file)
                        .param("tenantId", "tenant-demo")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("U101"))
                .andExpect(jsonPath("$.avatarObjectId").value("O9001"))
                .andExpect(jsonPath("$.avatarUrl").value("https://cdn.example.com/avatar/9001.png"));
    }

    @Test
    void shouldRedirectAvatarRequestToStorageAccessUrl() throws Exception {
        when(tenantRequestResolver.resolveTenantId("tenant-demo")).thenReturn(TENANT_ID);
        when(userApplicationService.getAvatarAccessUrl(TENANT_ID, "U101"))
                .thenReturn(Optional.of("https://cdn.example.com/avatar/9001.png"));

        mockMvc.perform(get("/upms/users/{userId}/avatar", "U101")
                        .param("tenantId", "tenant-demo"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://cdn.example.com/avatar/9001.png"));
    }
}
