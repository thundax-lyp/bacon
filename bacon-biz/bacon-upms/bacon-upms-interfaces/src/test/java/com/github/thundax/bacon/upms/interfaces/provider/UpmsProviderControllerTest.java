package com.github.thundax.bacon.upms.interfaces.provider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.upms.application.command.UserPasswordApplicationService;
import com.github.thundax.bacon.upms.application.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.application.dto.TenantDTO;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.application.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.application.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.application.query.DepartmentQueryApplicationService;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UpmsProviderControllerTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "upms-token";

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldExposeRawCurrentUserProviderPayload() throws Exception {
        UserQueryApplicationService userQueryApplicationService = mock(UserQueryApplicationService.class);
        DepartmentQueryApplicationService departmentQueryApplicationService = mock(DepartmentQueryApplicationService.class);
        when(userQueryApplicationService.getById(any()))
                .thenReturn(new UserDTO(
                        2001L,
                        "alice",
                        "Alice",
                        "storage-20260327100000-000501",
                        "13800000001",
                        11L,
                        "https://cdn.example.com/avatar.png",
                        "ACTIVE"));
        when(departmentQueryApplicationService.getById(any()))
                .thenReturn(new DepartmentDTO(11L, "RD", "研发部", 0L, 2001L, 1, "ACTIVE"));
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        newMockMvc(
                        userQueryApplicationService,
                        mock(UserPasswordApplicationService.class),
                        departmentQueryApplicationService,
                        mock(PermissionQueryApplicationService.class))
                .perform(get("/providers/upms/queries/current-user").header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2001))
                .andExpect(jsonPath("$.account").value("alice"))
                .andExpect(jsonPath("$.departmentCode").value("RD"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeRawCurrentTenantAndDataScopeProviderPayload() throws Exception {
        UserQueryApplicationService userQueryApplicationService = mock(UserQueryApplicationService.class);
        PermissionQueryApplicationService permissionQueryApplicationService = mock(PermissionQueryApplicationService.class);
        when(userQueryApplicationService.getTenantById(any()))
                .thenReturn(new TenantDTO(
                        1001L, "Demo Tenant", "TENANT_DEMO", "ACTIVE", Instant.parse("2099-01-01T00:00:00Z")));
        when(permissionQueryApplicationService.getUserDataScope(any()))
                .thenReturn(new UserDataScopeDTO(false, Set.of("DEPARTMENT"), Set.of(11L)));
        MockMvc mockMvc = newMockMvc(
                userQueryApplicationService,
                mock(UserPasswordApplicationService.class),
                mock(DepartmentQueryApplicationService.class),
                permissionQueryApplicationService);
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/upms/queries/current-tenant").header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TENANT_DEMO"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(get("/providers/upms/queries/current-data-scope")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allAccess").value(false))
                .andExpect(jsonPath("$.scopeTypes[0]").value("DEPARTMENT"))
                .andExpect(jsonPath("$.departmentIds[0]").value(11));
    }

    @Test
    void shouldExposeRawIdentityAndCredentialProviderPayload() throws Exception {
        UserQueryApplicationService userQueryApplicationService = mock(UserQueryApplicationService.class);
        when(userQueryApplicationService.getUserIdentity(any()))
                .thenReturn(new UserIdentityDTO(301L, 2001L, "ACCOUNT", "alice", "ACTIVE"));
        when(userQueryApplicationService.getUserLoginCredential(any()))
                .thenReturn(new UserLoginCredentialDTO(
                        2001L,
                        301L,
                        "alice",
                        "13800000001",
                        "ACCOUNT",
                        "alice",
                        "ACTIVE",
                        401L,
                        "PASSWORD",
                        "ACTIVE",
                        true,
                        Instant.parse("2099-01-01T00:00:00Z"),
                        null,
                        true,
                        List.of("TOTP"),
                        "ACTIVE",
                        "{noop}password"));
        MockMvc mockMvc = newMockMvc(
                userQueryApplicationService,
                mock(UserPasswordApplicationService.class),
                mock(DepartmentQueryApplicationService.class),
                mock(PermissionQueryApplicationService.class));
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/upms/queries/user-identity")
                        .param("identityType", "ACCOUNT")
                        .param("identityValue", "alice")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identityValue").value("alice"))
                .andExpect(jsonPath("$.code").doesNotExist());
        mockMvc.perform(get("/providers/upms/queries/user-credential")
                        .param("identityType", "ACCOUNT")
                        .param("identityValue", "alice")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentialId").value(401))
                .andExpect(jsonPath("$.needChangePassword").value(true))
                .andExpect(jsonPath("$.secondFactorTypes[0]").value("TOTP"));
    }

    @Test
    void shouldAcceptPasswordChangeProviderCommand() throws Exception {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        newMockMvc(
                        mock(UserQueryApplicationService.class),
                        mock(UserPasswordApplicationService.class),
                        mock(DepartmentQueryApplicationService.class),
                        mock(PermissionQueryApplicationService.class))
                .perform(post("/providers/upms/commands/change-current-user-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "oldPassword": "old-password",
                                  "newPassword": "new-password"
                                }
                                """)
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenProviderRequiredParamMissing() throws Exception {
        newMockMvc(
                        mock(UserQueryApplicationService.class),
                        mock(UserPasswordApplicationService.class),
                        mock(DepartmentQueryApplicationService.class),
                        mock(PermissionQueryApplicationService.class))
                .perform(get("/providers/upms/queries/user-identity").header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectProviderCallWhenTokenMissing() throws Exception {
        newMockMvc(
                        mock(UserQueryApplicationService.class),
                        mock(UserPasswordApplicationService.class),
                        mock(DepartmentQueryApplicationService.class),
                        mock(PermissionQueryApplicationService.class))
                .perform(get("/providers/upms/queries/current-user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProviderCallWhenTokenInvalid() throws Exception {
        newMockMvc(
                        mock(UserQueryApplicationService.class),
                        mock(UserPasswordApplicationService.class),
                        mock(DepartmentQueryApplicationService.class),
                        mock(PermissionQueryApplicationService.class))
                .perform(get("/providers/upms/queries/current-user")
                        .header(PROVIDER_TOKEN_HEADER, "wrong-token"))
                .andExpect(status().isForbidden());
    }

    private MockMvc newMockMvc(
            UserQueryApplicationService userQueryApplicationService,
            UserPasswordApplicationService userPasswordApplicationService,
            DepartmentQueryApplicationService departmentQueryApplicationService,
            PermissionQueryApplicationService permissionQueryApplicationService) {
        return MockMvcBuilders.standaloneSetup(new UpmsProviderController(
                        userQueryApplicationService,
                        userPasswordApplicationService,
                        departmentQueryApplicationService,
                        permissionQueryApplicationService))
                .addInterceptors(providerGuardInterceptor())
                .build();
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/upms/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }
}
