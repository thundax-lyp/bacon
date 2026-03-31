package com.github.thundax.bacon.upms.interfaces.controller;

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
        when(tenantRequestResolver.resolveTenantId("tenant-demo")).thenReturn(1001L);
        when(userApplicationService.updateAvatar(eq(1001L), eq(101L), eq("avatar.png"), eq("image/png"), eq(4L),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new UserDTO(101L, "tenant-demo", "alice", "Alice", 9001L, "13800000001", 11L,
                        "https://cdn.example.com/avatar/9001.png", "ENABLED"));

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3, 4});

        mockMvc.perform(multipart("/upms/users/{userId}/avatar", 101L)
                        .file(file)
                        .param("tenantNo", "tenant-demo")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.avatarObjectId").value(9001L))
                .andExpect(jsonPath("$.avatarUrl").value("https://cdn.example.com/avatar/9001.png"));
    }

    @Test
    void shouldRedirectAvatarRequestToStorageAccessUrl() throws Exception {
        when(tenantRequestResolver.resolveTenantId("tenant-demo")).thenReturn(1001L);
        when(userApplicationService.getAvatarAccessUrl(1001L, 101L))
                .thenReturn(Optional.of("https://cdn.example.com/avatar/9001.png"));

        mockMvc.perform(get("/upms/users/{userId}/avatar", 101L)
                        .param("tenantNo", "tenant-demo"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://cdn.example.com/avatar/9001.png"));
    }
}
