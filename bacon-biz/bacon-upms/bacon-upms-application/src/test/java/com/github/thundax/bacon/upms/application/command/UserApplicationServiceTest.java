package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserCredentialId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.dto.UserPageQueryDTO;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    private static final TenantId TENANT_ID = TenantId.of(1001L);
    private static final DepartmentId DEPARTMENT_ID = DepartmentId.of(11L);

    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private SessionCommandFacade sessionCommandFacade;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StoredObjectFacade storedObjectFacade;

    private UserApplicationService service;

    @BeforeEach
    void setUp() {
        service = new UserApplicationService(departmentRepository, userRepository, roleRepository, tenantRepository, sessionCommandFacade,
                passwordEncoder, storedObjectFacade);
    }

    @Test
    void shouldUploadAvatarToStorageAndReplaceOldReference() throws Exception {
        User currentUser = new User(UserId.of(101L), TENANT_ID, "Alice", StoredObjectId.of(301L),
                DEPARTMENT_ID, UserStatus.ENABLED, null, null, null, null);
        User savedUser = new User(UserId.of(101L), TENANT_ID, "Alice", StoredObjectId.of(401L),
                DEPARTMENT_ID, UserStatus.ENABLED, null, null, null, null);
        StoredObjectDTO storedObject = new StoredObjectDTO();
        storedObject.setId(StoredObjectId.of(401L));
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/401.png");

        when(userRepository.findUserById(TENANT_ID, UserId.of(101L))).thenReturn(Optional.of(currentUser));
        mockIdentity(UserId.of(101L), UserIdentityType.ACCOUNT, "alice");
        mockIdentity(UserId.of(101L), UserIdentityType.PHONE, "13800000001");
        when(storedObjectFacade.uploadObject(any())).thenReturn(storedObject);
        when(userRepository.save(any(User.class), any(), any())).thenReturn(savedUser);

        UserDTO result = service.updateAvatar(TENANT_ID, 101L, "avatar.png", "image/png", 1024L,
                new ByteArrayInputStream(createImageBytes("png", 256, 256)));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture(), org.mockito.ArgumentMatchers.eq("alice"),
                org.mockito.ArgumentMatchers.eq("13800000001"));
        verify(storedObjectFacade).markObjectReferenced("O401", "UPMS_USER_AVATAR", "101");
        verify(storedObjectFacade).clearObjectReference("O301", "UPMS_USER_AVATAR", "101");
        assertThat(userCaptor.getValue().getAvatarObjectId()).isEqualTo(StoredObjectId.of(401L));
        assertThat(result.getAvatarObjectId()).isEqualTo(401L);
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar/401.png");
    }

    @Test
    void shouldRejectUnsupportedAvatarContentType() {
        User currentUser = new User(UserId.of(101L), TENANT_ID, "Alice", null,
                DEPARTMENT_ID, UserStatus.ENABLED, null, null, null, null);
        when(userRepository.findUserById(TENANT_ID, UserId.of(101L))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(TENANT_ID, 101L, "avatar.gif", "image/gif", 12L,
                new ByteArrayInputStream(new byte[]{1, 2, 3})))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("avatar contentType must be image/jpeg or image/png");
    }

    @Test
    void shouldRejectNonSquareAvatarImage() throws Exception {
        User currentUser = new User(UserId.of(101L), TENANT_ID, "Alice", null,
                DEPARTMENT_ID, UserStatus.ENABLED, null, null, null, null);
        byte[] bytes = createImageBytes("png", 256, 180);
        when(userRepository.findUserById(TENANT_ID, UserId.of(101L))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(TENANT_ID, 101L, "avatar.png", "image/png", (long) bytes.length,
                new ByteArrayInputStream(bytes)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("avatar image must be square");
    }

    @Test
    void shouldNotResolveAvatarUrlWhenPagingUsers() {
        User user = new User(UserId.of(101L), TENANT_ID, "Alice", StoredObjectId.of(501L),
                DEPARTMENT_ID, UserStatus.ENABLED, null, null, null, null);
        when(userRepository.pageUsers(TENANT_ID, null, null, null, null, 1, 20)).thenReturn(List.of(user));
        when(userRepository.countUsers(TENANT_ID, null, null, null, null)).thenReturn(1L);
        mockIdentity(UserId.of(101L), UserIdentityType.ACCOUNT, "alice");
        mockIdentity(UserId.of(101L), UserIdentityType.PHONE, "13800000001");

        PageResultDTO<UserDTO> result = service.pageUsers(new UserPageQueryDTO(TENANT_ID, null, null, null, null, 1, 20));

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTenantId()).isEqualTo(1001L);
        assertThat(result.getRecords().get(0).getAvatarObjectId()).isEqualTo(501L);
        assertThat(result.getRecords().get(0).getAvatarUrl()).isNull();
        verify(storedObjectFacade, never()).getObjectById(any());
    }

    @Test
    void shouldClearAvatarReferenceWhenDeletingUser() {
        User user = new User(UserId.of(101L), TENANT_ID, "Alice", StoredObjectId.of(501L),
                DEPARTMENT_ID, UserStatus.ENABLED, null, null, null, null);
        when(userRepository.findUserById(TENANT_ID, UserId.of(101L))).thenReturn(Optional.of(user));

        service.deleteUser(TENANT_ID, 101L);

        verify(userRepository).deleteUser(TENANT_ID, UserId.of(101L));
        verify(storedObjectFacade).clearObjectReference("O501", "UPMS_USER_AVATAR", "101");
        verify(sessionCommandFacade).invalidateUserSessions(1001L, 101L, "USER_DELETED");
    }

    @Test
    void shouldResolveAvatarAccessUrl() {
        User user = new User(UserId.of(101L), TENANT_ID, "Alice", StoredObjectId.of(501L),
                DEPARTMENT_ID, UserStatus.ENABLED, null, null, null, null);
        StoredObjectDTO storedObject = new StoredObjectDTO();
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/501.png");
        when(userRepository.findUserById(TENANT_ID, UserId.of(101L))).thenReturn(Optional.of(user));
        when(storedObjectFacade.getObjectById("O501")).thenReturn(storedObject);

        assertThat(service.getAvatarAccessUrl(TENANT_ID, 101L)).contains("https://cdn.example.com/avatar/501.png");
    }

    @Test
    void shouldReturnLoginCredentialPasswordFromAccountIdentity() {
        User user = new User(UserId.of(101L), TENANT_ID, "Alice", null,
                DEPARTMENT_ID, UserStatus.ENABLED, null, null, null, null);
        UserIdentity accountIdentity = new UserIdentity(UserIdentityId.of(201L), TENANT_ID, UserId.of(101L),
                UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.ACTIVE, null, null, null, null);
        UserIdentity phoneIdentity = new UserIdentity(UserIdentityId.of(202L), TENANT_ID, UserId.of(101L),
                UserIdentityType.PHONE, "13800000001", UserIdentityStatus.ACTIVE, null, null, null, null);
        UserCredential passwordCredential = new UserCredential(UserCredentialId.of(301L), TENANT_ID, UserId.of(101L),
                UserIdentityId.of(201L), UserCredentialType.PASSWORD, UserCredentialFactorLevel.PRIMARY,
                "{noop}identity", UserCredentialStatus.ACTIVE, true, 0, 5, null, null, null, null,
                null, null, null, null);
        when(tenantRepository.findTenantByTenantId(TENANT_ID))
                .thenReturn(Optional.of(new Tenant(1001L, "Demo Tenant", "TENANT_DEMO",
                        TenantStatus.ACTIVE, Instant.parse("2099-01-01T00:00:00Z"), null, null, null, null)));
        when(userRepository.findUserIdentity(TENANT_ID, UserIdentityType.ACCOUNT, "alice"))
                .thenReturn(Optional.of(accountIdentity));
        when(userRepository.findUserIdentityByUserId(TENANT_ID, UserId.of(101L), UserIdentityType.ACCOUNT))
                .thenReturn(Optional.of(accountIdentity));
        when(userRepository.findUserIdentityByUserId(TENANT_ID, UserId.of(101L), UserIdentityType.PHONE))
                .thenReturn(Optional.of(phoneIdentity));
        when(userRepository.findUserCredential(TENANT_ID, UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.of(passwordCredential));
        when(userRepository.findUserById(TENANT_ID, UserId.of(101L))).thenReturn(Optional.of(user));

        UserLoginCredentialDTO credential = service.getUserLoginCredential(1001L, "ACCOUNT", "alice");

        assertThat(credential.getTenantId()).isEqualTo(1001L);
        assertThat(credential.getUserId()).isEqualTo(101L);
        assertThat(credential.getIdentityId()).isEqualTo(201L);
        assertThat(credential.getCredentialId()).isEqualTo(301L);
        assertThat(credential.getPasswordHash()).isEqualTo("{noop}identity");
        assertThat(credential.isNeedChangePassword()).isTrue();
    }

    @Test
    void shouldValidateOldPasswordAgainstAccountIdentity() {
        User user = new User(UserId.of(101L), TENANT_ID, "Alice", null,
                DEPARTMENT_ID, UserStatus.ENABLED, null, null, null, null);
        UserCredential passwordCredential = new UserCredential(UserCredentialId.of(301L), TENANT_ID, UserId.of(101L),
                UserIdentityId.of(201L), UserCredentialType.PASSWORD, UserCredentialFactorLevel.PRIMARY,
                "{noop}identity", UserCredentialStatus.ACTIVE, false, 0, 5, null, null, null, null,
                null, null, null, null);
        when(userRepository.findUserById(TENANT_ID, UserId.of(101L))).thenReturn(Optional.of(user));
        when(userRepository.findUserCredential(TENANT_ID, UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.of(passwordCredential));
        when(passwordEncoder.matches("old-password", "{noop}identity")).thenReturn(true);
        when(tenantRepository.findTenantByTenantId(TENANT_ID))
                .thenReturn(Optional.of(new Tenant(1001L, "Demo Tenant", "TENANT_DEMO",
                        TenantStatus.ACTIVE, Instant.parse("2099-01-01T00:00:00Z"), null, null, null, null)));

        service.changePassword(1001L, 101L, "old-password", "new-password");

        verify(userRepository).updatePassword(TENANT_ID, UserId.of(101L), "new-password", false);
    }

    private void mockIdentity(UserId userId, UserIdentityType identityType, String identityValue) {
        when(userRepository.findUserIdentityByUserId(TENANT_ID, userId, identityType))
                .thenReturn(Optional.of(new UserIdentity(
                        UserIdentityId.of(identityType == UserIdentityType.ACCOUNT ? 10001L : 10002L),
                        TENANT_ID, userId, identityType, identityValue, UserIdentityStatus.ACTIVE,
                        null, null, null, null)));
    }

    private byte[] createImageBytes(String format, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }
}
