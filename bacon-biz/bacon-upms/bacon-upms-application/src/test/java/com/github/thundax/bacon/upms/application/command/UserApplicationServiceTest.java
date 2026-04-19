package com.github.thundax.bacon.upms.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.UserCredentialRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRoleRepository;
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
    private UserCredentialRepository userCredentialRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private UserIdentityRepository userIdentityRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SessionCommandFacade sessionCommandFacade;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StoredObjectCommandFacade storedObjectCommandFacade;
    @Mock
    private StoredObjectReadFacade storedObjectReadFacade;

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
                userIdentityRepository,
                userCredentialRepository,
                userRoleRepository,
                sessionCommandFacade,
                passwordEncoder,
                storedObjectCommandFacade,
                storedObjectReadFacade,
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
        User currentUser = user(
                101L, "Alice", AvatarStoredObjectNo.of("storage-20260327100000-000301"), DEPARTMENT_ID, UserStatus.ACTIVE);
        User savedUser = user(
                101L, "Alice", AvatarStoredObjectNo.of("storage-20260327100000-000401"), DEPARTMENT_ID, UserStatus.ACTIVE);
        StoredObjectFacadeResponse storedObject = new StoredObjectFacadeResponse();
        storedObject.setStoredObjectNo("storage-20260327100000-000401");
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/401.png");

        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(currentUser));
        mockIdentity(UserId.of(101L), UserIdentityType.ACCOUNT, "alice");
        mockIdentity(UserId.of(101L), UserIdentityType.PHONE, "13800000001");
        when(storedObjectCommandFacade.uploadObject(any())).thenReturn(storedObject);
        when(userRepository.update(any(User.class), any(), any(), any(), any(), any()))
                .thenReturn(savedUser);

        UserDTO result = service.updateAvatar(
                UserId.of(101L),
                "avatar.png",
                "image/png",
                1024L,
                new ByteArrayInputStream(createImageBytes("png", 256, 256)));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository)
                .update(
                        userCaptor.capture(),
                        org.mockito.ArgumentMatchers.eq("alice"),
                        org.mockito.ArgumentMatchers.eq("13800000001"),
                        any(),
                        any(),
                        any());
        verify(storedObjectCommandFacade)
                .markObjectReferenced(
                        new StoredObjectReferenceFacadeRequest(
                                "storage-20260327100000-000401", "UPMS_USER_AVATAR", "101"));
        verify(storedObjectCommandFacade)
                .clearObjectReference(
                        new StoredObjectReferenceFacadeRequest(
                                "storage-20260327100000-000301", "UPMS_USER_AVATAR", "101"));
        assertThat(userCaptor.getValue().getAvatarStoredObjectNo())
                .isEqualTo(AvatarStoredObjectNo.of("storage-20260327100000-000401"));
        assertThat(result.getAvatarStoredObjectNo()).isEqualTo("storage-20260327100000-000401");
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar/401.png");
    }

    @Test
    void shouldRejectUnsupportedAvatarContentType() {
        User currentUser = user(101L, "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(
                        UserId.of(101L), "avatar.gif", "image/gif", 12L, new ByteArrayInputStream(new byte[] {1, 2, 3
                        })))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("avatar contentType must be image/jpeg or image/png");
    }

    @Test
    void shouldRejectNonSquareAvatarImage() throws Exception {
        User currentUser = user(101L, "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        byte[] bytes = createImageBytes("png", 256, 180);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(
                        UserId.of(101L),
                        "avatar.png",
                        "image/png",
                        (long) bytes.length,
                        new ByteArrayInputStream(bytes)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("avatar image must be square");
    }

    @Test
    void shouldClearAvatarReferenceWhenDeletingUser() {
        User user = user(
                101L, "Alice", AvatarStoredObjectNo.of("storage-20260327100000-000501"), DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));

        service.delete(UserId.of(101L));

        verify(userRepository).delete(UserId.of(101L));
        verify(storedObjectCommandFacade)
                .clearObjectReference(
                        new StoredObjectReferenceFacadeRequest(
                                "storage-20260327100000-000501", "UPMS_USER_AVATAR", "101"));
        verify(sessionCommandFacade)
                .invalidateUserSessions(new SessionInvalidateUserFacadeRequest(1001L, 101L, "USER_DELETED"));
    }

    @Test
    void shouldValidateOldPasswordAgainstAccountIdentity() {
        User user = user(101L, "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        UserCredential passwordCredential = credential(301L, 101L, 201L, "{noop}identity", false);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));
        when(userCredentialRepository.findCredentialByUserId(UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.of(passwordCredential));
        when(passwordEncoder.matches("old-password", "{noop}identity")).thenReturn(true);
        service.changePassword(UserId.of(101L), "old-password", "new-password");

        verify(userRepository).updatePassword(UserId.of(101L), "new-password", false, UserCredentialId.of(10003L));
    }

    private static User user(
            Long id,
            String name,
            AvatarStoredObjectNo avatarStoredObjectNo,
            DepartmentId departmentId,
            UserStatus status) {
        return User.reconstruct(UserId.of(id), name, avatarStoredObjectNo, departmentId, status);
    }

    private void mockIdentity(UserId userId, UserIdentityType identityType, String identityValue) {
        when(userIdentityRepository.findIdentityByUserId(userId, identityType))
                .thenReturn(Optional.of(com.github.thundax.bacon.upms.domain.model.entity.UserIdentity.create(
                        identityType == UserIdentityType.ACCOUNT ? UserIdentityId.of(10001L) : UserIdentityId.of(10002L),
                        userId,
                        identityType,
                        identityValue)));
    }

    private static UserCredential credential(
            Long id, Long userId, Long identityId, String credentialValue, boolean needChangePassword) {
        return UserCredential.createPassword(
                UserCredentialId.of(id),
                UserId.of(userId),
                UserIdentityId.of(identityId),
                credentialValue,
                needChangePassword,
                5,
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
