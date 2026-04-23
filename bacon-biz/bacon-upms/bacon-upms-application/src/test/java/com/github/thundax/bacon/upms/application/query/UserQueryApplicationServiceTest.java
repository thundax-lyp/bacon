package com.github.thundax.bacon.upms.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.upms.application.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.dto.TenantDTO;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.application.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import com.github.thundax.bacon.upms.domain.repository.UserCredentialRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryApplicationServiceTest {

    private static final DepartmentId DEPARTMENT_ID = DepartmentId.of(11L);

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserIdentityRepository userIdentityRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private StoredObjectReadFacade storedObjectReadFacade;

    private UserQueryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new UserQueryApplicationService(
                userRepository,
                userIdentityRepository,
                userCredentialRepository,
                roleRepository,
                tenantRepository,
                storedObjectReadFacade);
    }

    @Test
    void shouldGetByIdWithAvatarUrl() {
        User user = user(
                101L, "Alice", AvatarStoredObjectNo.of("storage-20260327100000-000501"), DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));
        mockIdentity(UserId.of(101L), UserIdentityType.ACCOUNT, "alice");
        mockIdentity(UserId.of(101L), UserIdentityType.PHONE, "13800000001");
        StoredObjectFacadeResponse storedObject = new StoredObjectFacadeResponse(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "https://cdn.example.com/avatar/501.png",
                null,
                null,
                null);
        when(storedObjectReadFacade.getObjectByNo(argThat(request ->
                        request != null && "storage-20260327100000-000501".equals(request.getStoredObjectNo()))))
                .thenReturn(storedObject);

        UserDTO result = service.getById(UserId.of(101L));

        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getAccount()).isEqualTo("alice");
        assertThat(result.getPhone()).isEqualTo("13800000001");
        assertThat(result.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar/501.png");
    }

    @Test
    void shouldGetUserIdentityByAccount() {
        UserIdentity accountIdentity = identity(201L, 101L, UserIdentityType.ACCOUNT, "alice");
        when(userIdentityRepository.findIdentity(UserIdentityType.ACCOUNT, "alice"))
                .thenReturn(Optional.of(accountIdentity));

        UserIdentityDTO result = service.getUserIdentity(new UserIdentityQuery(UserIdentityType.ACCOUNT, "alice"));

        assertThat(result.getId()).isEqualTo(201L);
        assertThat(result.getUserId()).isEqualTo(101L);
        assertThat(result.getIdentityType()).isEqualTo("ACCOUNT");
        assertThat(result.getIdentityValue()).isEqualTo("alice");
    }

    @Test
    void shouldReturnLoginCredentialPasswordFromAccountIdentity() {
        User user = user(101L, "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        UserIdentity accountIdentity = identity(201L, 101L, UserIdentityType.ACCOUNT, "alice");
        UserIdentity phoneIdentity = identity(202L, 101L, UserIdentityType.PHONE, "13800000001");
        UserCredential passwordCredential = credential(301L, 101L, 201L, "{noop}identity", true);
        when(userIdentityRepository.findIdentity(UserIdentityType.ACCOUNT, "alice"))
                .thenReturn(Optional.of(accountIdentity));
        when(userIdentityRepository.findIdentityByUserId(UserId.of(101L), UserIdentityType.ACCOUNT))
                .thenReturn(Optional.of(accountIdentity));
        when(userIdentityRepository.findIdentityByUserId(UserId.of(101L), UserIdentityType.PHONE))
                .thenReturn(Optional.of(phoneIdentity));
        when(userCredentialRepository.findCredentialByUserId(UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.of(passwordCredential));
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));

        UserLoginCredentialDTO credential =
                service.getUserLoginCredential(new UserLoginCredentialQuery(UserIdentityType.ACCOUNT, "alice"));

        assertThat(credential.getUserId()).isEqualTo(101L);
        assertThat(credential.getIdentityId()).isEqualTo(201L);
        assertThat(credential.getCredentialId()).isEqualTo(301L);
        assertThat(credential.getPasswordHash()).isEqualTo("{noop}identity");
        assertThat(credential.isNeedChangePassword()).isTrue();
    }

    @Test
    void shouldRejectExpiredLoginCredential() {
        UserIdentity accountIdentity = identity(201L, 101L, UserIdentityType.ACCOUNT, "alice");
        UserCredential passwordCredential = UserCredential.createPassword(
                com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId.of(301L),
                UserId.of(101L),
                com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId.of(201L),
                "{noop}identity",
                false,
                5,
                Instant.parse("2000-01-01T00:00:00Z"));
        when(userIdentityRepository.findIdentity(UserIdentityType.ACCOUNT, "alice"))
                .thenReturn(Optional.of(accountIdentity));
        when(userCredentialRepository.findCredentialByUserId(UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.of(passwordCredential));

        assertThatThrownBy(
                        () -> service.getUserLoginCredential(
                                new UserLoginCredentialQuery(UserIdentityType.ACCOUNT, "alice")))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User credential is expired");
    }

    @Test
    void shouldNotResolveAvatarUrlWhenPagingUsers() {
        User user = user(
                101L, "Alice", AvatarStoredObjectNo.of("storage-20260327100000-000501"), DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.page(null, null, null, null, 1, 20)).thenReturn(List.of(user));
        when(userRepository.count(null, null, null, null)).thenReturn(1L);
        mockIdentity(UserId.of(101L), UserIdentityType.ACCOUNT, "alice");
        mockIdentity(UserId.of(101L), UserIdentityType.PHONE, "13800000001");

        PageResult<UserDTO> result = service.page(new UserPageQuery(null, null, null, null, 1, 20));

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getAvatarStoredObjectNo()).isEqualTo("storage-20260327100000-000501");
        assertThat(result.getRecords().get(0).getAvatarUrl()).isNull();
        verify(storedObjectReadFacade, never()).getObjectByNo(any());
    }

    @Test
    void shouldResolveAvatarAccessUrl() {
        User user = user(
                101L, "Alice", AvatarStoredObjectNo.of("storage-20260327100000-000501"), DEPARTMENT_ID, UserStatus.ACTIVE);
        StoredObjectFacadeResponse storedObject = new StoredObjectFacadeResponse(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "https://cdn.example.com/avatar/501.png",
                null,
                null,
                null);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));
        when(storedObjectReadFacade.getObjectByNo(argThat(request ->
                        request != null && "storage-20260327100000-000501".equals(request.getStoredObjectNo()))))
                .thenReturn(storedObject);

        assertThat(service.getAvatarAccessUrl(UserId.of(101L))).contains("https://cdn.example.com/avatar/501.png");
    }

    @Test
    void shouldGetRolesByUserId() {
        User user = user(101L, "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));
        when(roleRepository.findByUserId(UserId.of(101L)))
                .thenReturn(List.of(Role.create(
                        RoleId.of(11L),
                        RoleCode.of("ADMIN"),
                        "管理员",
                        RoleType.SYSTEM_ROLE,
                        RoleDataScopeType.SELF)));

        List<RoleDTO> roles = service.getRolesByUserId(UserId.of(101L));

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getId()).isEqualTo(11L);
        assertThat(roles.get(0).getCode()).isEqualTo("ADMIN");
    }

    @Test
    void shouldExportUsersWithoutAvatarUrlLookup() {
        User user = user(
                101L, "Alice", AvatarStoredObjectNo.of("storage-20260327100000-000501"), DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.list(null, null, null, null)).thenReturn(List.of(user));
        mockIdentity(UserId.of(101L), UserIdentityType.ACCOUNT, "alice");
        mockIdentity(UserId.of(101L), UserIdentityType.PHONE, "13800000001");

        List<UserDTO> users = service.exportUsers(new UserExportQuery(null, null, null, null));

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getAvatarUrl()).isNull();
        verify(storedObjectReadFacade, never()).getObjectByNo(any());
    }

    @Test
    void shouldGetTenantById() {
        when(tenantRepository.findById(TenantId.of(1001L)))
                .thenReturn(Optional.of(tenant(1001L, "租户A", "TENANT_A", TenantStatus.ACTIVE, null)));

        TenantDTO result = service.getTenantById(TenantId.of(1001L));

        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getName()).isEqualTo("租户A");
        assertThat(result.getCode()).isEqualTo("TENANT_A");
    }

    @Test
    void shouldRejectMissingUserWhenGettingRoles() {
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRolesByUserId(UserId.of(101L)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found: 101");
    }

    private void mockIdentity(UserId userId, UserIdentityType identityType, String identityValue) {
        when(userIdentityRepository.findIdentityByUserId(userId, identityType))
                .thenReturn(Optional.of(identity(
                        identityType == UserIdentityType.ACCOUNT ? 10001L : 10002L,
                        userId.value(),
                        identityType,
                        identityValue)));
    }

    private static Tenant tenant(Long id, String name, String code, TenantStatus status, Instant expiredAt) {
        return Tenant.reconstruct(TenantId.of(id), name, TenantCode.of(code), status, expiredAt);
    }

    private static User user(
            Long id,
            String name,
            AvatarStoredObjectNo avatarStoredObjectNo,
            DepartmentId departmentId,
            UserStatus status) {
        return User.reconstruct(UserId.of(id), name, avatarStoredObjectNo, departmentId, status);
    }

    private static UserIdentity identity(Long id, Long userId, UserIdentityType type, String value) {
        return UserIdentity.create(
                com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId.of(id),
                UserId.of(userId),
                type,
                value);
    }

    private static UserCredential credential(
            Long id, Long userId, Long identityId, String credentialValue, boolean needChangePassword) {
        return UserCredential.createPassword(
                com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId.of(id),
                UserId.of(userId),
                com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId.of(identityId),
                credentialValue,
                needChangePassword,
                5,
                (Instant) null);
    }
}
