package com.github.thundax.bacon.upms.interfaces.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.request.UserCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserCredentialFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpmsFacadeLocalContractTest {

    @Mock
    private UserQueryApplicationService userQueryApplicationService;

    @Mock
    private DepartmentQueryApplicationService departmentQueryApplicationService;

    @Mock
    private PermissionQueryApplicationService permissionQueryApplicationService;

    @Mock
    private UserPasswordApplicationService userPasswordApplicationService;

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldMapCurrentUserFacadeToApplicationQueries() {
        when(userQueryApplicationService.getById(UserId.of(2001L)))
                .thenReturn(new UserDTO(
                        2001L,
                        "alice",
                        "Alice",
                        "storage-20260327100000-000501",
                        "13800000001",
                        11L,
                        "https://cdn.example.com/avatar.png",
                        "ACTIVE"));
        when(departmentQueryApplicationService.getById(argThat(departmentId ->
                        departmentId != null && departmentId.value().equals(11L))))
                .thenReturn(new DepartmentDTO(11L, "RD", "研发部", 0L, 2001L, 1, "ACTIVE"));

        CurrentUserReadFacadeLocalImpl facade = new CurrentUserReadFacadeLocalImpl(
                userQueryApplicationService, departmentQueryApplicationService, permissionQueryApplicationService);

        UserFacadeResponse response = facade.getCurrentUser();

        assertThat(response.id()).isEqualTo(2001L);
        assertThat(response.account()).isEqualTo("alice");
        assertThat(response.departmentCode()).isEqualTo("RD");
        assertThat(response.avatarUrl()).isEqualTo("https://cdn.example.com/avatar.png");
    }

    @Test
    void shouldMapCurrentTenantAndDataScopeFacadeToApplicationQueries() {
        when(userQueryApplicationService.getTenantById(argThat(tenantId ->
                        tenantId != null && tenantId.value().equals(1001L))))
                .thenReturn(new TenantDTO(
                        1001L, "Demo Tenant", "TENANT_DEMO", "ACTIVE", Instant.parse("2099-01-01T00:00:00Z")));
        when(permissionQueryApplicationService.getUserDataScope(UserId.of(2001L)))
                .thenReturn(new UserDataScopeDTO(false, Set.of("DEPARTMENT"), Set.of(11L)));
        CurrentUserReadFacadeLocalImpl facade = new CurrentUserReadFacadeLocalImpl(
                userQueryApplicationService, departmentQueryApplicationService, permissionQueryApplicationService);

        TenantFacadeResponse tenant = facade.getCurrentTenant();
        UserDataScopeFacadeResponse dataScope = facade.getCurrentDataScope();

        assertThat(tenant.code()).isEqualTo("TENANT_DEMO");
        assertThat(tenant.status()).isEqualTo("ACTIVE");
        assertThat(dataScope.allAccess()).isFalse();
        assertThat(dataScope.scopeTypes()).containsExactly("DEPARTMENT");
        assertThat(dataScope.departmentIds()).containsExactly(11L);
    }

    @Test
    void shouldMapUserCredentialFacadeToApplicationQueries() {
        when(userQueryApplicationService.getUserIdentity(argThat(query ->
                        query != null && query.identityType().value().equals("ACCOUNT") && query.identityValue().equals("alice"))))
                .thenReturn(new UserIdentityDTO(301L, 2001L, "ACCOUNT", "alice", "ACTIVE"));
        when(userQueryApplicationService.getUserLoginCredential(argThat(query ->
                        query != null && query.identityType().value().equals("ACCOUNT") && query.identityValue().equals("alice"))))
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
        UserCredentialReadFacadeLocalImpl facade = new UserCredentialReadFacadeLocalImpl(userQueryApplicationService);

        UserIdentityFacadeResponse identity =
                facade.getUserIdentity(new UserIdentityGetFacadeRequest("ACCOUNT", "alice"));
        UserCredentialFacadeResponse credential =
                facade.getUserCredential(new UserCredentialGetFacadeRequest("ACCOUNT", "alice"));

        assertThat(identity.identityValue()).isEqualTo("alice");
        assertThat(credential.credentialId()).isEqualTo(401L);
        assertThat(credential.secondFactorTypes()).containsExactly("TOTP");
    }

    @Test
    void shouldMapUserPasswordFacadeToApplicationCommand() {
        UserPasswordFacadeLocalImpl facade = new UserPasswordFacadeLocalImpl(userPasswordApplicationService);

        facade.changePassword(new UserPasswordChangeFacadeRequest("old-password", "new-password"));

        verify(userPasswordApplicationService)
                .changePassword(argThat(command -> command != null
                        && command.userId().equals(UserId.of(2001L))
                        && command.oldPassword().equals("old-password")
                        && command.newPassword().equals("new-password")));
    }
}
