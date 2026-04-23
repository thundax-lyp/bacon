package com.github.thundax.bacon.upms.application.command;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionCacheRepository;
import com.github.thundax.bacon.upms.domain.repository.UserCredentialRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRoleRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserProfileApplicationServiceTest {

    private static final TenantId TENANT_ID = TenantId.of(1001L);
    private static final DepartmentId DEPARTMENT_ID = DepartmentId.of(11L);

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private PermissionCacheRepository permissionCacheRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCredentialRepository userCredentialRepository;
    @Mock
    private UserIdentityRepository userIdentityRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private SessionCommandFacade sessionCommandFacade;
    @Mock
    private StoredObjectCommandFacade storedObjectCommandFacade;
    @Mock
    private StoredObjectReadFacade storedObjectReadFacade;
    @Mock
    private Ids ids;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserProfileApplicationService service;

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContextHolder.BaconContext(TENANT_ID.value(), 2001L));
        service = new UserProfileApplicationService(
                departmentRepository,
                permissionCacheRepository,
                userRepository,
                userCredentialRepository,
                userIdentityRepository,
                userRoleRepository,
                sessionCommandFacade,
                storedObjectCommandFacade,
                storedObjectReadFacade,
                ids,
                idGenerator,
                passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldClearAvatarReferenceWhenDeletingUser() {
        User user = User.reconstruct(
                UserId.of(101L),
                "Alice",
                AvatarStoredObjectNo.of("storage-20260327100000-000501"),
                DEPARTMENT_ID,
                UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));

        service.delete(UserId.of(101L));

        verify(userRepository).delete(UserId.of(101L));
        verify(storedObjectCommandFacade)
                .clearObjectReference(argThat(request -> request != null
                        && "storage-20260327100000-000501".equals(request.getStoredObjectNo())
                        && "UPMS_USER_AVATAR".equals(request.getOwnerType())
                        && "101".equals(request.getOwnerId())));
        verify(sessionCommandFacade)
                .invalidateUserSessions(argThat(request -> request != null
                        && request.getTenantId().equals(1001L)
                        && request.getUserId().equals(101L)
                        && request.getReason().equals("USER_DELETED")));
    }

    @Test
    void shouldReplaceAccountAndPhoneIdentityInApplicationWhenUpdatingUser() {
        User currentUser = User.reconstruct(UserId.of(101L), "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        UserIdentity accountIdentity =
                UserIdentity.create(UserIdentityId.of(201L), UserId.of(101L), UserIdentityType.ACCOUNT, "alice");
        UserIdentity phoneIdentity =
                UserIdentity.create(UserIdentityId.of(202L), UserId.of(101L), UserIdentityType.PHONE, "13800000001");
        UserCredential passwordCredential = UserCredential.createPassword(
                UserCredentialId.of(301L),
                UserId.of(101L),
                accountIdentity.getId(),
                "encoded-old",
                true,
                5,
                Instant.now().plusSeconds(3600));
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(currentUser));
        when(userRepository.findByAccount("alice-new")).thenReturn(Optional.empty());
        when(userRepository.update(currentUser)).thenReturn(currentUser);
        when(userIdentityRepository.findIdentityByUserId(UserId.of(101L), UserIdentityType.ACCOUNT))
                .thenReturn(Optional.of(accountIdentity));
        when(userIdentityRepository.findIdentityByUserId(UserId.of(101L), UserIdentityType.PHONE))
                .thenReturn(Optional.of(phoneIdentity));
        when(userIdentityRepository.update(accountIdentity)).thenAnswer(invocation -> invocation.getArgument(0));
        when(userIdentityRepository.update(phoneIdentity)).thenAnswer(invocation -> invocation.getArgument(0));
        when(userCredentialRepository.findCredentialByUserId(UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.of(passwordCredential), Optional.of(passwordCredential));
        when(userCredentialRepository.update(passwordCredential)).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(new UserUpdateCommand(
                UserId.of(101L), "alice-new", "Alice Zhang", "13900000001", DEPARTMENT_ID));

        verify(userRepository).update(currentUser);
        verify(userIdentityRepository).update(accountIdentity);
        verify(userIdentityRepository).update(phoneIdentity);
        verify(userCredentialRepository).update(passwordCredential);
    }

    @Test
    void shouldEvictUserPermissionCacheAfterUpdatingRoleIds() {
        User user = User.reconstruct(UserId.of(101L), "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));
        when(userRoleRepository.updateRoleIds(UserId.of(101L), java.util.List.of(RoleId.of(301L))))
                .thenReturn(java.util.List.of());

        service.updateRoleIds(new UserRoleAssignCommand(UserId.of(101L), java.util.List.of(RoleId.of(301L))));

        verify(permissionCacheRepository).deleteUserPermission(TENANT_ID, UserId.of(101L));
    }
}
