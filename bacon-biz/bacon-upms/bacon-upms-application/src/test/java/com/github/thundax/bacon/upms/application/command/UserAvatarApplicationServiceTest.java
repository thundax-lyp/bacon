package com.github.thundax.bacon.upms.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAvatarApplicationServiceTest {

    private static final DepartmentId DEPARTMENT_ID = DepartmentId.of(11L);

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserIdentityRepository userIdentityRepository;
    @Mock
    private StoredObjectCommandFacade storedObjectCommandFacade;
    @Mock
    private IdGenerator idGenerator;

    private UserAvatarApplicationService service;

    @BeforeEach
    void setUp() {
        service = new UserAvatarApplicationService(
                userRepository, userIdentityRepository, storedObjectCommandFacade, idGenerator);
    }

    @Test
    void shouldUploadAvatarToStorageAndReplaceOldReference() throws Exception {
        User currentUser = User.reconstruct(
                UserId.of(101L),
                "Alice",
                AvatarStoredObjectNo.of("storage-20260327100000-000301"),
                DEPARTMENT_ID,
                UserStatus.ACTIVE);
        User savedUser = User.reconstruct(
                UserId.of(101L),
                "Alice",
                AvatarStoredObjectNo.of("storage-20260327100000-000401"),
                DEPARTMENT_ID,
                UserStatus.ACTIVE);
        StoredObjectFacadeResponse storedObject = new StoredObjectFacadeResponse();
        storedObject.setStoredObjectNo("storage-20260327100000-000401");
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/401.png");

        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(currentUser));
        when(idGenerator.nextId("user-identity-id")).thenReturn(10001L, 10002L);
        when(idGenerator.nextId("user-credential-id")).thenReturn(10003L);
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
        User currentUser = User.reconstruct(UserId.of(101L), "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(
                        UserId.of(101L), "avatar.gif", "image/gif", 12L, new ByteArrayInputStream(new byte[] {1, 2, 3
                        })))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("avatar contentType must be image/jpeg or image/png");
    }

    @Test
    void shouldRejectNonSquareAvatarImage() throws Exception {
        User currentUser = User.reconstruct(UserId.of(101L), "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
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

    private void mockIdentity(UserId userId, UserIdentityType identityType, String identityValue) {
        when(userIdentityRepository.findIdentityByUserId(userId, identityType))
                .thenReturn(Optional.of(com.github.thundax.bacon.upms.domain.model.entity.UserIdentity.create(
                        identityType == UserIdentityType.ACCOUNT ? UserIdentityId.of(10001L) : UserIdentityId.of(10002L),
                        userId,
                        identityType,
                        identityValue)));
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
