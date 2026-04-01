package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserCredentialId;
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
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
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

    private static final TenantId TENANT_ID = TenantId.of("tenant-demo");
    private static final DepartmentId DEPARTMENT_ID = DepartmentId.of("D11");

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
        User currentUser = new User(UserId.of("U101"), TENANT_ID, "alice", "Alice", 301L, "13800000001", "{noop}123456",
                DEPARTMENT_ID,
                UserStatus.ENABLED);
        User savedUser = new User(UserId.of("U101"), TENANT_ID, "alice", "Alice", 401L, "13800000001", "{noop}123456",
                DEPARTMENT_ID,
                UserStatus.ENABLED);
        StoredObjectDTO storedObject = new StoredObjectDTO();
        storedObject.setId(401L);
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/401.png");

        when(userRepository.findUserById(TENANT_ID, UserId.of("U101"))).thenReturn(Optional.of(currentUser));
        when(storedObjectFacade.uploadObject(any())).thenReturn(storedObject);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = service.updateAvatar(TENANT_ID, "U101", "avatar.png", "image/png", 1024L,
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
        User currentUser = new User(UserId.of("U101"), TENANT_ID, "alice", "Alice", null, "13800000001", "{noop}123456",
                DEPARTMENT_ID,
                UserStatus.ENABLED);
        when(userRepository.findUserById(TENANT_ID, UserId.of("U101"))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(TENANT_ID, "U101", "avatar.gif", "image/gif", 12L,
                new ByteArrayInputStream(new byte[]{1, 2, 3})))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("avatar contentType must be image/jpeg or image/png");
    }

    @Test
    void shouldRejectNonSquareAvatarImage() throws Exception {
        User currentUser = new User(UserId.of("U101"), TENANT_ID, "alice", "Alice", null, "13800000001", "{noop}123456",
                DEPARTMENT_ID,
                UserStatus.ENABLED);
        byte[] bytes = createImageBytes("png", 256, 180);
        when(userRepository.findUserById(TENANT_ID, UserId.of("U101"))).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateAvatar(TENANT_ID, "U101", "avatar.png", "image/png", (long) bytes.length,
                new ByteArrayInputStream(bytes)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("avatar image must be square");
    }

    @Test
    void shouldNotResolveAvatarUrlWhenPagingUsers() {
        User user = new User(UserId.of("U101"), TENANT_ID, "alice", "Alice", 501L, "13800000001", "{noop}123456",
                DEPARTMENT_ID,
                UserStatus.ENABLED);
        when(userRepository.pageUsers(TENANT_ID, null, null, null, null, 1, 20)).thenReturn(List.of(user));
        when(userRepository.countUsers(TENANT_ID, null, null, null, null)).thenReturn(1L);

        UserPageResultDTO result = service.pageUsers(new UserPageQueryDTO(TENANT_ID, null, null, null, null, 1, 20));

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTenantId()).isEqualTo("tenant-demo");
        assertThat(result.getRecords().get(0).getAvatarObjectId()).isEqualTo(501L);
        assertThat(result.getRecords().get(0).getAvatarUrl()).isNull();
        verify(storedObjectFacade, never()).getObjectById(any());
    }

    @Test
    void shouldClearAvatarReferenceWhenDeletingUser() {
        User user = new User(UserId.of("U101"), TENANT_ID, "alice", "Alice", 501L, "13800000001", "{noop}123456",
                DEPARTMENT_ID,
                UserStatus.ENABLED);
        when(userRepository.findUserById(TENANT_ID, UserId.of("U101"))).thenReturn(Optional.of(user));

        service.deleteUser(TENANT_ID, "U101");

        verify(userRepository).deleteUser(TENANT_ID, UserId.of("U101"));
        verify(storedObjectFacade).clearObjectReference(501L, "UPMS_USER_AVATAR", "U101");
        verify(sessionCommandFacade).invalidateUserSessions("tenant-demo", "U101", "USER_DELETED");
    }

    @Test
    void shouldResolveAvatarAccessUrl() {
        User user = new User(UserId.of("U101"), TENANT_ID, "alice", "Alice", 501L, "13800000001", "{noop}123456",
                DEPARTMENT_ID,
                UserStatus.ENABLED);
        StoredObjectDTO storedObject = new StoredObjectDTO();
        storedObject.setAccessEndpoint("https://cdn.example.com/avatar/501.png");
        when(userRepository.findUserById(TENANT_ID, UserId.of("U101"))).thenReturn(Optional.of(user));
        when(storedObjectFacade.getObjectById(501L)).thenReturn(storedObject);

        assertThat(service.getAvatarAccessUrl(TENANT_ID, "U101")).contains("https://cdn.example.com/avatar/501.png");
    }

    @Test
    void shouldReturnLoginCredentialPasswordFromAccountIdentity() {
        User user = new User(UserId.of("U101"), TENANT_ID, "alice", "Alice", null, "13800000001", "{noop}user",
                DEPARTMENT_ID,
                UserStatus.ENABLED);
        UserIdentity accountIdentity = new UserIdentity(201L, TENANT_ID, UserId.of("U101"), "ACCOUNT", "alice", true);
        UserCredential passwordCredential = new UserCredential(UserCredentialId.of("C301"), TENANT_ID, UserId.of("U101"), 201L, "PASSWORD", "PRIMARY",
                "{noop}identity", UserCredentialStatus.ACTIVE, true, 0, 5, null, null, null, null);
        when(tenantRepository.findTenantByTenantId(TENANT_ID))
                .thenReturn(Optional.of(new Tenant("tenant-demo", "Demo Tenant", "TENANT_DEMO",
                        TenantStatus.ACTIVE, Instant.parse("2099-01-01T00:00:00Z"))));
        when(userRepository.findUserIdentity(TENANT_ID, "ACCOUNT", "alice")).thenReturn(Optional.of(accountIdentity));
        when(userRepository.findUserCredential(TENANT_ID, UserId.of("U101"), "PASSWORD")).thenReturn(Optional.of(passwordCredential));
        when(userRepository.findUserById(TENANT_ID, UserId.of("U101"))).thenReturn(Optional.of(user));

        UserLoginCredentialDTO credential = service.getUserLoginCredential("tenant-demo", "ACCOUNT", "alice");

        assertThat(credential.getTenantId()).isEqualTo("tenant-demo");
        assertThat(credential.getUserId()).isEqualTo("U101");
        assertThat(credential.getCredentialId()).isEqualTo("C301");
        assertThat(credential.getPasswordHash()).isEqualTo("{noop}identity");
        assertThat(credential.isNeedChangePassword()).isTrue();
    }

    @Test
    void shouldValidateOldPasswordAgainstAccountIdentity() {
        User user = new User(UserId.of("U101"), TENANT_ID, "alice", "Alice", null, "13800000001", "{noop}user",
                DEPARTMENT_ID,
                UserStatus.ENABLED);
        UserCredential passwordCredential = new UserCredential(UserCredentialId.of("C301"), TENANT_ID, UserId.of("U101"), 201L, "PASSWORD", "PRIMARY",
                "{noop}identity", UserCredentialStatus.ACTIVE, false, 0, 5, null, null, null, null);
        when(userRepository.findUserById(TENANT_ID, UserId.of("U101"))).thenReturn(Optional.of(user));
        when(userRepository.findUserCredential(TENANT_ID, UserId.of("U101"), "PASSWORD")).thenReturn(Optional.of(passwordCredential));
        when(passwordEncoder.matches("old-password", "{noop}identity")).thenReturn(true);
        when(tenantRepository.findTenantByTenantId(TENANT_ID))
                .thenReturn(Optional.of(new Tenant("tenant-demo", "Demo Tenant", "TENANT_DEMO",
                        TenantStatus.ACTIVE, Instant.parse("2099-01-01T00:00:00Z"))));

        service.changePassword("tenant-demo", "U101", "old-password", "new-password");

        verify(userRepository).updatePassword(TENANT_ID, UserId.of("U101"), "new-password", false);
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
