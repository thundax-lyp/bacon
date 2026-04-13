package com.github.thundax.bacon.upms.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.dto.UserPageQueryDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private Ids ids;

    @Mock
    private IdGenerator idGenerator;

    private UserApplicationService service;

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContextHolder.BaconContext(TENANT_ID.value(), 2001L));
        service = new UserApplicationService(
                departmentRepository,
                userRepository,
                roleRepository,
                tenantRepository,
                sessionCommandFacade,
                passwordEncoder,
                storedObjectFacade,
                ids,
                idGenerator);
        lenient().when(idGenerator.nextId("user-identity-id")).thenReturn(10001L, 10002L, 10011L, 10012L);
        lenient().when(idGenerator.nextId("user-credential-id")).thenReturn(10003L, 10013L, 10023L);
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldUploadAvatarToStorageAndReplaceOldReference() throws Exception {
        User currentUser = user(101L, "Alice", StoredObjectId.of(301L), DEPARTMENT_ID, UserStatus.ENABLED);
        User savedUser = user(101L, "Alice", StoredObjectId.of(401L), DEPARTMENT_ID, UserStatus.ENABLED);
        StoredObjectDTO storedObject = new StoredObjectDTO();
        storedObject.setId(StoredObjectId.of(401L));
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/401.png");

        when(userRepository.findUserById(UserId.of(101L))).thenReturn(Optional.of(currentUser));
        mockIdentity(UserId.of(101L), UserIdentityType.ACCOUNT, "alice");
        mockIdentity(UserId.of(101L), UserIdentityType.PHONE, "13800000001");
        when(storedObjectFacade.uploadObject(any())).thenReturn(storedObject);
        when(userRepository.save(any(User.class), any(), any(), any(), any(), any())).thenReturn(savedUser);

        UserDTO result = service.updateAvatar(
                101L, "avatar.png", "image/png", 1024L, new ByteArrayInputStream(createImageBytes("png", 256, 256)));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository)
                .save(
                        userCaptor.capture(),
                        org.mockito.ArgumentMatchers.eq("alice"),
                        org.mockito.ArgumentMatchers.eq("13800000001"),
                        any(),
                        any(),
                        any());
        verify(storedObjectFacade).markObjectReferenced("O401", "UPMS_USER_AVATAR", "101");
        verify(storedObjectFacade).clearObjectReference("O301", "UPMS_USER_AVATAR", "101");
        assertThat(userCaptor.getValue().getAvatarObjectId()).isEqualTo(StoredObjectId.of(401L));
        assertThat(result.getAvatarObjectId()).isEqualTo(401L);
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar/401.png");
    }

    @Test
    void shouldRejectUnsupportedAvatarContentType() {
        User currentUser = user(101L, "Alice", null, DEPARTMENT_ID, UserStatus.ENABLED);
        when(userRepository.findUserById(UserId.of(101L))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(
                        () -> service.updateAvatar(101L, "avatar.gif", "image/gif", 12L, new ByteArrayInputStream(new byte[] {1, 2, 3})))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("avatar contentType must be image/jpeg or image/png");
    }

    @Test
    void shouldRejectNonSquareAvatarImage() throws Exception {
        User currentUser = user(101L, "Alice", null, DEPARTMENT_ID, UserStatus.ENABLED);
        byte[] bytes = createImageBytes("png", 256, 180);
        when(userRepository.findUserById(UserId.of(101L))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(101L, "avatar.png", "image/png", (long) bytes.length, new ByteArrayInputStream(bytes)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("avatar image must be square");
    }

    @Test
    void shouldNotResolveAvatarUrlWhenPagingUsers() {
        User user = user(101L, "Alice", StoredObjectId.of(501L), DEPARTMENT_ID, UserStatus.ENABLED);
        when(userRepository.pageUsers(null, null, null, null, 1, 20)).thenReturn(List.of(user));
        when(userRepository.countUsers(null, null, null, null)).thenReturn(1L);
        mockIdentity(UserId.of(101L), UserIdentityType.ACCOUNT, "alice");
        mockIdentity(UserId.of(101L), UserIdentityType.PHONE, "13800000001");

        PageResultDTO<UserDTO> result = service.pageUsers(new UserPageQueryDTO(null, null, null, null, 1, 20));

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getAvatarObjectId()).isEqualTo(501L);
        assertThat(result.getRecords().get(0).getAvatarUrl()).isNull();
        verify(storedObjectFacade, never()).getObjectById(any());
    }

    @Test
    void shouldClearAvatarReferenceWhenDeletingUser() {
        User user = user(101L, "Alice", StoredObjectId.of(501L), DEPARTMENT_ID, UserStatus.ENABLED);
        when(userRepository.findUserById(UserId.of(101L))).thenReturn(Optional.of(user));

        service.deleteUser(101L);

        verify(userRepository).deleteUser(UserId.of(101L));
        verify(storedObjectFacade).clearObjectReference("O501", "UPMS_USER_AVATAR", "101");
        verify(sessionCommandFacade).invalidateUserSessions(1001L, 101L, "USER_DELETED");
    }

    @Test
    void shouldResolveAvatarAccessUrl() {
        User user = user(101L, "Alice", StoredObjectId.of(501L), DEPARTMENT_ID, UserStatus.ENABLED);
        StoredObjectDTO storedObject = new StoredObjectDTO();
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/501.png");
        when(userRepository.findUserById(UserId.of(101L))).thenReturn(Optional.of(user));
        when(storedObjectFacade.getObjectById("O501")).thenReturn(storedObject);

        assertThat(service.getAvatarAccessUrl(101L)).contains("https://cdn.example.com/avatar/501.png");
    }

    @Test
    void shouldReturnLoginCredentialPasswordFromAccountIdentity() {
        User user = user(101L, "Alice", null, DEPARTMENT_ID, UserStatus.ENABLED);
        UserIdentity accountIdentity = identity(201L, 101L, UserIdentityType.ACCOUNT, "alice");
        UserIdentity phoneIdentity = identity(202L, 101L, UserIdentityType.PHONE, "13800000001");
        UserCredential passwordCredential =
                credential(301L, 101L, 201L, "{noop}identity", true);
        when(userRepository.findUserIdentity(UserIdentityType.ACCOUNT, "alice"))
                .thenReturn(Optional.of(accountIdentity));
        when(userRepository.findUserIdentityByUserId(UserId.of(101L), UserIdentityType.ACCOUNT))
                .thenReturn(Optional.of(accountIdentity));
        when(userRepository.findUserIdentityByUserId(UserId.of(101L), UserIdentityType.PHONE))
                .thenReturn(Optional.of(phoneIdentity));
        when(userRepository.findUserCredential(UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.of(passwordCredential));
        when(userRepository.findUserById(UserId.of(101L))).thenReturn(Optional.of(user));

        UserLoginCredentialDTO credential = service.getUserLoginCredential("ACCOUNT", "alice");

        assertThat(credential.getUserId()).isEqualTo(101L);
        assertThat(credential.getIdentityId()).isEqualTo(201L);
        assertThat(credential.getCredentialId()).isEqualTo(301L);
        assertThat(credential.getPasswordHash()).isEqualTo("{noop}identity");
        assertThat(credential.isNeedChangePassword()).isTrue();
    }

    @Test
    void shouldValidateOldPasswordAgainstAccountIdentity() {
        User user = user(101L, "Alice", null, DEPARTMENT_ID, UserStatus.ENABLED);
        UserCredential passwordCredential =
                credential(301L, 101L, 201L, "{noop}identity", false);
        when(userRepository.findUserById(UserId.of(101L))).thenReturn(Optional.of(user));
        when(userRepository.findUserCredential(UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.of(passwordCredential));
        when(passwordEncoder.matches("old-password", "{noop}identity")).thenReturn(true);
        service.changePassword(101L, "old-password", "new-password");

        verify(userRepository).updatePassword(UserId.of(101L), "new-password", false, UserCredentialId.of(10003L));
    }

    private void mockIdentity(UserId userId, UserIdentityType identityType, String identityValue) {
        when(userRepository.findUserIdentityByUserId(userId, identityType))
                .thenReturn(Optional.of(identity(
                        identityType == UserIdentityType.ACCOUNT ? 10001L : 10002L,
                        userId.value(),
                        identityType,
                        identityValue)));
    }

    private static Tenant tenant(Long id, String name, String code, TenantStatus status, Instant expiredAt) {
        return Tenant.create(TenantId.of(id), name, TenantCode.of(code), status, expiredAt);
    }

    private static User user(Long id, String name, StoredObjectId avatarObjectId, DepartmentId departmentId, UserStatus status) {
        return User.create(UserId.of(id), name, avatarObjectId, departmentId, status);
    }

    private static UserIdentity identity(Long id, Long userId, UserIdentityType type, String value) {
        return UserIdentity.create(UserIdentityId.of(id), UserId.of(userId), type, value, UserIdentityStatus.ACTIVE);
    }

    private static UserCredential credential(
            Long id, Long userId, Long identityId, String credentialValue, boolean needChangePassword) {
        return UserCredential.create(
                UserCredentialId.of(id),
                UserId.of(userId),
                UserIdentityId.of(identityId),
                UserCredentialType.PASSWORD,
                UserCredentialFactorLevel.PRIMARY,
                credentialValue,
                UserCredentialStatus.ACTIVE,
                needChangePassword,
                0,
                5,
                (String) null,
                (Instant) null,
                (Instant) null,
                (Instant) null);
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
