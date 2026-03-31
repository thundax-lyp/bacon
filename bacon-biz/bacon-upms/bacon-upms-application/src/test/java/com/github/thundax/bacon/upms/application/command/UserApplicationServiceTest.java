package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.dto.UserPageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.UserPageResultDTO;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    @Mock
    private UserRepository userRepository;
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
        service = new UserApplicationService(userRepository, roleRepository, tenantRepository, sessionCommandFacade,
                passwordEncoder, storedObjectFacade);
    }

    @Test
    void shouldUploadAvatarToStorageAndReplaceOldReference() throws Exception {
        User currentUser = new User(UserId.of("U101"), 1001L, "alice", "Alice", 301L, "13800000001", "{noop}123456", 11L,
                UserStatus.ENABLED);
        User savedUser = new User(UserId.of("U101"), 1001L, "alice", "Alice", 401L, "13800000001", "{noop}123456", 11L,
                UserStatus.ENABLED);
        StoredObjectDTO storedObject = new StoredObjectDTO();
        storedObject.setId(401L);
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/401.png");

        when(userRepository.findUserById(1001L, UserId.of("U101"))).thenReturn(Optional.of(currentUser));
        when(tenantRepository.findTenantById(1001L))
                .thenReturn(Optional.of(new Tenant(1001L, "tenant-demo", "Demo Tenant", TenantStatus.ENABLED)));
        when(storedObjectFacade.uploadObject(any())).thenReturn(storedObject);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = service.updateAvatar(1001L, "U101", "avatar.png", "image/png", 1024L,
                new ByteArrayInputStream(createImageBytes("png", 256, 256)));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(storedObjectFacade).markObjectReferenced(401L, "UPMS_USER_AVATAR", "U101");
        verify(storedObjectFacade).clearObjectReference(301L, "UPMS_USER_AVATAR", "U101");
        assertThat(userCaptor.getValue().getAvatarObjectId()).isEqualTo(401L);
        assertThat(result.getAvatarObjectId()).isEqualTo(401L);
        assertThat(result.getId()).isEqualTo("U101");
        assertThat(result.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar/401.png");
    }

    @Test
    void shouldRejectUnsupportedAvatarContentType() {
        User currentUser = new User(UserId.of("U101"), 1001L, "alice", "Alice", null, "13800000001", "{noop}123456", 11L,
                UserStatus.ENABLED);
        when(userRepository.findUserById(1001L, UserId.of("U101"))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(1001L, "U101", "avatar.gif", "image/gif", 12L,
                new ByteArrayInputStream(new byte[]{1, 2, 3})))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("avatar contentType must be image/jpeg or image/png");
    }

    @Test
    void shouldRejectNonSquareAvatarImage() throws Exception {
        User currentUser = new User(UserId.of("U101"), 1001L, "alice", "Alice", null, "13800000001", "{noop}123456", 11L,
                UserStatus.ENABLED);
        byte[] bytes = createImageBytes("png", 256, 180);
        when(userRepository.findUserById(1001L, UserId.of("U101"))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(1001L, "U101", "avatar.png", "image/png", (long) bytes.length,
                new ByteArrayInputStream(bytes)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("avatar image must be square");
    }

    @Test
    void shouldNotResolveAvatarUrlWhenPagingUsers() {
        User user = new User(UserId.of("U101"), 1001L, "alice", "Alice", 501L, "13800000001", "{noop}123456", 11L,
                UserStatus.ENABLED);
        when(userRepository.pageUsers(1001L, null, null, null, null, 1, 20)).thenReturn(List.of(user));
        when(userRepository.countUsers(1001L, null, null, null, null)).thenReturn(1L);
        when(tenantRepository.findTenantById(1001L))
                .thenReturn(Optional.of(new Tenant(1001L, "tenant-demo", "Demo Tenant", TenantStatus.ENABLED)));

        UserPageResultDTO result = service.pageUsers(new UserPageQueryDTO(1001L, null, null, null, null, 1, 20));

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTenantNo()).isEqualTo("tenant-demo");
        assertThat(result.getRecords().get(0).getAvatarObjectId()).isEqualTo(501L);
        assertThat(result.getRecords().get(0).getAvatarUrl()).isNull();
        verify(storedObjectFacade, never()).getObjectById(any());
    }

    @Test
    void shouldClearAvatarReferenceWhenDeletingUser() {
        User user = new User(UserId.of("U101"), 1001L, "alice", "Alice", 501L, "13800000001", "{noop}123456", 11L,
                UserStatus.ENABLED);
        when(userRepository.findUserById(1001L, UserId.of("U101"))).thenReturn(Optional.of(user));

        service.deleteUser(1001L, "U101");

        verify(userRepository).deleteUser(1001L, UserId.of("U101"));
        verify(storedObjectFacade).clearObjectReference(501L, "UPMS_USER_AVATAR", "U101");
        verify(sessionCommandFacade).invalidateUserSessions("1001", "U101", "USER_DELETED");
    }

    @Test
    void shouldResolveAvatarAccessUrl() {
        User user = new User(UserId.of("U101"), 1001L, "alice", "Alice", 501L, "13800000001", "{noop}123456", 11L,
                UserStatus.ENABLED);
        StoredObjectDTO storedObject = new StoredObjectDTO();
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/501.png");
        when(userRepository.findUserById(1001L, UserId.of("U101"))).thenReturn(Optional.of(user));
        when(storedObjectFacade.getObjectById(501L)).thenReturn(storedObject);

        assertThat(service.getAvatarAccessUrl(1001L, "U101")).contains("https://cdn.example.com/avatar/501.png");
    }

    @Test
    void shouldReturnLoginCredentialPasswordFromAccountIdentity() {
        User user = new User(UserId.of("U101"), 1001L, "alice", "Alice", null, "13800000001", "{noop}user", 11L,
                UserStatus.ENABLED);
        UserIdentity accountIdentity = new UserIdentity(201L, 1001L, UserId.of("U101"), "ACCOUNT", "alice", true);
        UserCredential passwordCredential = new UserCredential(301L, 1001L, UserId.of("U101"), 201L, "PASSWORD", "PRIMARY",
                "{noop}identity", "ACTIVE", true, 0, 5, null, null, null, null);
        when(tenantRepository.findTenantByTenantId(TenantId.of("tenant-demo")))
                .thenReturn(Optional.of(new Tenant(1001L, "tenant-demo", "Demo Tenant", TenantStatus.ENABLED)));
        when(userRepository.findUserIdentity(1001L, "ACCOUNT", "alice")).thenReturn(Optional.of(accountIdentity));
        when(userRepository.findUserCredential(1001L, UserId.of("U101"), "PASSWORD")).thenReturn(Optional.of(passwordCredential));
        when(userRepository.findUserById(1001L, UserId.of("U101"))).thenReturn(Optional.of(user));
        when(tenantRepository.findTenantById(1001L))
                .thenReturn(Optional.of(new Tenant(1001L, "tenant-demo", "Demo Tenant", TenantStatus.ENABLED)));

        UserLoginCredentialDTO credential = service.getUserLoginCredential("tenant-demo", "ACCOUNT", "alice");

        assertThat(credential.getTenantNo()).isEqualTo("tenant-demo");
        assertThat(credential.getUserId()).isEqualTo("U101");
        assertThat(credential.getPasswordHash()).isEqualTo("{noop}identity");
        assertThat(credential.isNeedChangePassword()).isTrue();
    }

    @Test
    void shouldValidateOldPasswordAgainstAccountIdentity() {
        User user = new User(UserId.of("U101"), 1001L, "alice", "Alice", null, "13800000001", "{noop}user", 11L,
                UserStatus.ENABLED);
        UserCredential passwordCredential = new UserCredential(301L, 1001L, UserId.of("U101"), 201L, "PASSWORD", "PRIMARY",
                "{noop}identity", "ACTIVE", false, 0, 5, null, null, null, null);
        when(userRepository.findUserById(1001L, UserId.of("U101"))).thenReturn(Optional.of(user));
        when(userRepository.findUserCredential(1001L, UserId.of("U101"), "PASSWORD")).thenReturn(Optional.of(passwordCredential));
        when(passwordEncoder.matches("old-password", "{noop}identity")).thenReturn(true);
        when(tenantRepository.findTenantByTenantId(TenantId.of("tenant-demo")))
                .thenReturn(Optional.of(new Tenant(1001L, "tenant-demo", "Demo Tenant", TenantStatus.ENABLED)));

        service.changePassword("tenant-demo", "U101", "old-password", "new-password");

        verify(userRepository).updatePassword(1001L, UserId.of("U101"), "new-password", false);
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
