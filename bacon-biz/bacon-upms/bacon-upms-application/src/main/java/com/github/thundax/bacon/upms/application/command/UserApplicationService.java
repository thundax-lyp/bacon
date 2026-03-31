package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.dto.UserPageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.UserPageResultDTO;
import com.github.thundax.bacon.upms.api.enums.UpmsStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantNo;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserApplicationService {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final String USER_AVATAR_OWNER_TYPE = "UPMS_USER_AVATAR";
    private static final String USER_AVATAR_CATEGORY = "avatar";
    private static final long MAX_AVATAR_SIZE = 2L * 1024L * 1024L;
    private static final int MIN_AVATAR_PIXEL = 128;
    private static final int MAX_AVATAR_PIXEL = 1024;
    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final SessionCommandFacade sessionCommandFacade;
    private final PasswordEncoder passwordEncoder;
    private final StoredObjectFacade storedObjectFacade;

    public UserApplicationService(UserRepository userRepository, RoleRepository roleRepository,
                                  TenantRepository tenantRepository, SessionCommandFacade sessionCommandFacade,
                                  PasswordEncoder passwordEncoder, StoredObjectFacade storedObjectFacade) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.sessionCommandFacade = sessionCommandFacade;
        this.passwordEncoder = passwordEncoder;
        this.storedObjectFacade = storedObjectFacade;
    }

    public UserDTO getUserById(Long tenantId, Long userId) {
        return toDetailedDto(requireUser(tenantId, userId));
    }

    public UserDTO getUserById(String tenantNo, Long userId) {
        return getUserById(resolveTenantIdByTenantNo(tenantNo), userId);
    }

    public UserIdentityDTO getUserIdentity(Long tenantId, String identityType, String identityValue) {
        UserIdentity userIdentity = userRepository.findUserIdentity(tenantId, identityType, identityValue)
                .orElseThrow(() -> new IllegalArgumentException("User identity not found"));
        return new UserIdentityDTO(userIdentity.getId(), resolveTenantNoByTenantId(userIdentity.getTenantId()),
                userIdentity.getUserId(),
                userIdentity.getIdentityType(), userIdentity.getIdentityValue(), userIdentity.isEnabled());
    }

    public UserIdentityDTO getUserIdentity(String tenantNo, String identityType, String identityValue) {
        return getUserIdentity(resolveTenantIdByTenantNo(tenantNo), identityType, identityValue);
    }

    public UserLoginCredentialDTO getUserLoginCredential(Long tenantId, String identityType, String identityValue) {
        UserIdentity userIdentity = userRepository.findUserIdentity(tenantId, identityType, identityValue)
                .orElseThrow(() -> new IllegalArgumentException("User identity not found"));
        UserCredential passwordCredential = userRepository.findUserCredential(tenantId, userIdentity.getUserId(), "PASSWORD")
                .orElseThrow(() -> new IllegalArgumentException("Password credential not found"));
        User user = userRepository.findUserById(userIdentity.getTenantId(), userIdentity.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userIdentity.getUserId()));
        return new UserLoginCredentialDTO(resolveTenantNoByTenantId(user.getTenantId()), user.getId(),
                user.getAccount(), user.getPhone(),
                userIdentity.getIdentityType(), userIdentity.getIdentityValue(), userIdentity.isEnabled(),
                passwordCredential.getId(), passwordCredential.getCredentialType(), passwordCredential.getStatus(),
                passwordCredential.isNeedChangePassword(), passwordCredential.getExpiresAt(),
                passwordCredential.getLockedUntil(), false, List.of(), user.getStatus().value(),
                passwordCredential.getCredentialValue());
    }

    public UserLoginCredentialDTO getUserLoginCredential(String tenantNo, String identityType, String identityValue) {
        return getUserLoginCredential(resolveTenantIdByTenantNo(tenantNo), identityType, identityValue);
    }

    public TenantDTO getTenantByTenantNo(String tenantNo) {
        Tenant tenant = tenantRepository.findTenantByTenantNo(new TenantNo(tenantNo))
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantNo));
        return new TenantDTO(tenant.getId(), tenant.getTenantNo(), tenant.getName(), tenant.getStatus().value());
    }

    public UserPageResultDTO pageUsers(UserPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        String tenantNo = resolveTenantNoByTenantId(query.getTenantId());
        return new UserPageResultDTO(userRepository.pageUsers(query.getTenantId(), query.getAccount(), query.getName(),
                query.getPhone(), query.getStatus(), pageNo, pageSize).stream()
                .map(user -> toSummaryDto(user, tenantNo))
                .toList(),
                userRepository.countUsers(query.getTenantId(), query.getAccount(), query.getName(), query.getPhone(),
                        query.getStatus()),
                pageNo, pageSize);
    }

    public UserDTO createUser(Long tenantId, String account, String name, String phone, Long departmentId) {
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(tenantId, account, null);
        User savedUser = userRepository.save(new User(null, tenantId, normalize(account), normalize(name), normalize(phone),
                null, departmentId, UserStatus.ENABLED));
        return toDetailedDto(savedUser);
    }

    public UserDTO updateUser(Long tenantId, Long userId, String account, String name, String phone, Long departmentId) {
        User currentUser = requireUser(tenantId, userId);
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(tenantId, account, userId);
        User savedUser = userRepository.save(new User(
                currentUser.getId(),
                tenantId,
                normalize(account),
                normalize(name),
                currentUser.getAvatarObjectId(),
                normalize(phone),
                currentUser.getPasswordHash(),
                departmentId,
                currentUser.getStatus(),
                currentUser.getCreatedBy(),
                currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt()));
        return toDetailedDto(savedUser);
    }

    public UserDTO updateUserStatus(Long tenantId, Long userId, UpmsStatusEnum status) {
        User currentUser = requireUser(tenantId, userId);
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        User savedUser = userRepository.save(new User(
                currentUser.getId(),
                tenantId,
                currentUser.getAccount(),
                currentUser.getName(),
                currentUser.getAvatarObjectId(),
                currentUser.getPhone(),
                currentUser.getPasswordHash(),
                currentUser.getDepartmentId(),
                toDomainStatus(status),
                currentUser.getCreatedBy(),
                currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt()));
        if (UserStatus.DISABLED == savedUser.getStatus()) {
            sessionCommandFacade.invalidateUserSessions(String.valueOf(tenantId), userId, "USER_DISABLED");
        }
        return toDetailedDto(savedUser);
    }

    public Optional<String> getAvatarAccessUrl(Long tenantId, Long userId) {
        User user = requireUser(tenantId, userId);
        if (user.getAvatarObjectId() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(resolveAvatarUrl(user.getAvatarObjectId()));
    }

    public void deleteUser(Long tenantId, Long userId) {
        User currentUser = requireUser(tenantId, userId);
        userRepository.deleteUser(tenantId, userId);
        if (currentUser.getAvatarObjectId() != null) {
            storedObjectFacade.clearObjectReference(currentUser.getAvatarObjectId(), USER_AVATAR_OWNER_TYPE,
                    String.valueOf(userId));
        }
        sessionCommandFacade.invalidateUserSessions(String.valueOf(tenantId), userId, "USER_DELETED");
    }

    public UserDTO initPassword(Long tenantId, Long userId) {
        requireUser(tenantId, userId);
        User user = userRepository.updatePassword(tenantId, userId, DEFAULT_PASSWORD, true);
        sessionCommandFacade.invalidateUserSessions(String.valueOf(tenantId), userId, "USER_PASSWORD_INITIALIZED");
        return toDetailedDto(user);
    }

    public UserDTO resetPassword(Long tenantId, Long userId, String newPassword) {
        requireUser(tenantId, userId);
        validateRequired(newPassword, "newPassword");
        User user = userRepository.updatePassword(tenantId, userId, normalize(newPassword), true);
        sessionCommandFacade.invalidateUserSessions(String.valueOf(tenantId), userId, "USER_PASSWORD_RESET");
        return toDetailedDto(user);
    }

    public void changePassword(Long tenantId, Long userId, String oldPassword, String newPassword) {
        User user = requireUser(tenantId, userId);
        UserCredential passwordCredential = userRepository.findUserCredential(tenantId, userId, "PASSWORD")
                .orElseThrow(() -> new IllegalArgumentException("Password credential not found: " + userId));
        validateRequired(oldPassword, "oldPassword");
        validateRequired(newPassword, "newPassword");
        if (!passwordEncoder.matches(oldPassword, passwordCredential.getCredentialValue())) {
            throw new IllegalArgumentException("Old password invalid");
        }
        userRepository.updatePassword(tenantId, userId, normalize(newPassword), false);
    }

    public void changePassword(String tenantNo, Long userId, String oldPassword, String newPassword) {
        changePassword(resolveTenantIdByTenantNo(tenantNo), userId, oldPassword, newPassword);
    }

    public List<RoleDTO> assignRoles(Long tenantId, Long userId, List<Long> roleIds) {
        requireUser(tenantId, userId);
        String tenantNo = resolveTenantNoByTenantId(tenantId);
        return userRepository.assignRoles(tenantId, userId, roleIds).stream()
                .map(role -> toRoleDto(role, tenantNo))
                .toList();
    }

    public List<RoleDTO> getRolesByUserId(Long tenantId, Long userId) {
        requireUser(tenantId, userId);
        String tenantNo = resolveTenantNoByTenantId(tenantId);
        return roleRepository.findRolesByUserId(tenantId, userId).stream()
                .map(role -> toRoleDto(role, tenantNo))
                .toList();
    }

    public List<UserDTO> importUsers(Long tenantId, List<UserImportCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        return commands.stream()
                .map(command -> createUser(tenantId, command.account(), command.name(), command.phone(),
                        command.departmentId()))
                .toList();
    }

    public List<UserDTO> exportUsers(UserPageQueryDTO query) {
        String tenantNo = resolveTenantNoByTenantId(query.getTenantId());
        return userRepository.listUsers(query.getTenantId(), query.getAccount(), query.getName(), query.getPhone(),
                query.getStatus()).stream().map(user -> toSummaryDto(user, tenantNo)).toList();
    }

    public UserDTO updateAvatar(Long tenantId, Long userId, String originalFilename, String contentType, Long size,
                                InputStream inputStream) {
        User currentUser = requireUser(tenantId, userId);
        AvatarImage avatarImage = readAndValidateAvatar(originalFilename, contentType, size, inputStream);
        StoredObjectDTO storedObject = uploadAvatarObject(tenantId, avatarImage);
        storedObjectFacade.markObjectReferenced(storedObject.getId(), USER_AVATAR_OWNER_TYPE, String.valueOf(userId));
        Long previousAvatarObjectId = currentUser.getAvatarObjectId();
        try {
            User savedUser = userRepository.save(new User(
                    currentUser.getId(),
                    currentUser.getTenantId(),
                    currentUser.getAccount(),
                    currentUser.getName(),
                    storedObject.getId(),
                    currentUser.getPhone(),
                    currentUser.getPasswordHash(),
                    currentUser.getDepartmentId(),
                    currentUser.getStatus(),
                    currentUser.getCreatedBy(),
                    currentUser.getCreatedAt(),
                    currentUser.getUpdatedBy(),
                    currentUser.getUpdatedAt()));
            if (previousAvatarObjectId != null && !previousAvatarObjectId.equals(storedObject.getId())) {
                storedObjectFacade.clearObjectReference(previousAvatarObjectId, USER_AVATAR_OWNER_TYPE,
                        String.valueOf(userId));
            }
            return toDto(savedUser, storedObject.getAccessEndpoint(), resolveTenantNoByTenantId(savedUser.getTenantId()));
        } catch (RuntimeException ex) {
            storedObjectFacade.clearObjectReference(storedObject.getId(), USER_AVATAR_OWNER_TYPE, String.valueOf(userId));
            throw ex;
        }
    }

    private User requireUser(Long tenantId, Long userId) {
        return userRepository.findUserById(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private Long resolveTenantIdByTenantNo(String tenantNo) {
        validateRequired(tenantNo, "tenantNo");
        return tenantRepository.findTenantByTenantNo(new TenantNo(tenantNo))
                .map(Tenant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantNo));
    }

    private String resolveTenantNoByTenantId(Long tenantId) {
        return tenantRepository.findTenantById(tenantId)
                .map(Tenant::getTenantNo)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    private void ensureAccountUnique(Long tenantId, String account, Long excludedUserId) {
        userRepository.findUserByAccount(tenantId, normalize(account))
                .filter(existingUser -> !existingUser.getId().equals(excludedUserId))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("User account already exists: " + account);
                });
    }

    private UserDTO toDetailedDto(User user) {
        return toDto(user, resolveAvatarUrl(user.getAvatarObjectId()), resolveTenantNoByTenantId(user.getTenantId()));
    }

    private UserDTO toSummaryDto(User user, String tenantNo) {
        return toDto(user, null, tenantNo);
    }

    private UserDTO toDto(User user, String avatarUrl, String tenantNo) {
        return new UserDTO(user.getId(), tenantNo, user.getAccount(), user.getName(),
                user.getAvatarObjectId(), user.getPhone(), user.getDepartmentId(), avatarUrl, user.getStatus().value());
    }

    private RoleDTO toRoleDto(Role role, String tenantNo) {
        return new RoleDTO(role.getId(), tenantNo, role.getCode(), role.getName(),
                role.getRoleType(),
                role.getDataScopeType(), role.getStatus());
    }

    private AvatarImage readAndValidateAvatar(String originalFilename, String contentType, Long size, InputStream inputStream) {
        validateRequired(originalFilename, "originalFilename");
        if (inputStream == null) {
            throw new IllegalArgumentException("avatar file must not be null");
        }
        if (size == null || size <= 0L) {
            throw new IllegalArgumentException("avatar size must be greater than 0");
        }
        if (size > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("avatar size exceeds 2MB");
        }
        String normalizedContentType = normalizeContentType(contentType);
        if (!ALLOWED_AVATAR_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new IllegalArgumentException("avatar contentType must be image/jpeg or image/png");
        }
        try {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                throw new IllegalArgumentException("avatar file must not be empty");
            }
            if (bytes.length > MAX_AVATAR_SIZE) {
                throw new IllegalArgumentException("avatar size exceeds 2MB");
            }
            String actualContentType = detectAvatarContentType(bytes);
            if (!normalizedContentType.equals(actualContentType)) {
                throw new IllegalArgumentException("avatar contentType does not match image data");
            }
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
            if (bufferedImage == null) {
                throw new IllegalArgumentException("avatar file is not a valid image");
            }
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            if (width != height) {
                throw new IllegalArgumentException("avatar image must be square");
            }
            if (width < MIN_AVATAR_PIXEL || width > MAX_AVATAR_PIXEL) {
                throw new IllegalArgumentException("avatar image width must be between 128 and 1024");
            }
            if (height < MIN_AVATAR_PIXEL || height > MAX_AVATAR_PIXEL) {
                throw new IllegalArgumentException("avatar image height must be between 128 and 1024");
            }
            return new AvatarImage(originalFilename.trim(), actualContentType, (long) bytes.length, bytes);
        } catch (IOException ex) {
            throw new IllegalArgumentException("avatar file cannot be read", ex);
        }
    }

    private StoredObjectDTO uploadAvatarObject(Long tenantId, AvatarImage avatarImage) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(avatarImage.bytes())) {
            return storedObjectFacade.uploadObject(new UploadObjectCommand(
                    USER_AVATAR_OWNER_TYPE,
                    String.valueOf(tenantId),
                    USER_AVATAR_CATEGORY,
                    avatarImage.originalFilename(),
                    avatarImage.contentType(),
                    avatarImage.size(),
                    inputStream));
        } catch (IOException ex) {
            throw new IllegalStateException("failed to close avatar stream", ex);
        }
    }

    private String resolveAvatarUrl(Long avatarObjectId) {
        if (avatarObjectId == null) {
            return null;
        }
        StoredObjectDTO storedObject = storedObjectFacade.getObjectById(avatarObjectId);
        return storedObject == null ? null : storedObject.getAccessEndpoint();
    }

    private String detectAvatarContentType(byte[] bytes) throws IOException {
        ImageIO.setUseCache(false);
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (imageInputStream == null) {
                throw new IllegalArgumentException("avatar file is not a valid image");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("avatar file is not a supported image");
            }
            ImageReader reader = readers.next();
            try {
                String formatName = reader.getFormatName().toLowerCase(Locale.ROOT);
                if ("jpeg".equals(formatName) || "jpg".equals(formatName)) {
                    return "image/jpeg";
                }
                if ("png".equals(formatName)) {
                    return "image/png";
                }
                throw new IllegalArgumentException("avatar image format must be jpeg or png");
            } finally {
                reader.dispose();
            }
        }
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private UserStatus toDomainStatus(UpmsStatusEnum status) {
        return UserStatus.valueOf(status.name());
    }

    private record AvatarImage(String originalFilename, String contentType, Long size, byte[] bytes) {
    }
}
