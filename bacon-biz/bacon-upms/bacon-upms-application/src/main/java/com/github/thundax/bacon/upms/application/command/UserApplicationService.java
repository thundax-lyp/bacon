package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
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
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
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

    public UserDTO getUserById(TenantId tenantId, UserId userId) {
        return toDetailedDto(requireUser(tenantId, userId));
    }

    public UserDTO getUserById(String tenantId, String userId) {
        return getUserById(requireExistingTenantId(tenantId), UserId.of(userId));
    }

    public UserIdentityDTO getUserIdentity(TenantId tenantId, String identityType, String identityValue) {
        UserIdentity userIdentity = userRepository.findUserIdentity(tenantId, toIdentityType(identityType), identityValue)
                .orElseThrow(() -> new IllegalArgumentException("User identity not found"));
        return new UserIdentityDTO(userIdentity.getId() == null ? null : userIdentity.getId().value(),
                userIdentity.getTenantId().value(),
                userIdentity.getUserId().value(),
                userIdentity.getIdentityType().value(), userIdentity.getIdentityValue(), userIdentity.isEnabled());
    }

    public UserIdentityDTO getUserIdentity(String tenantId, String identityType, String identityValue) {
        return getUserIdentity(requireExistingTenantId(tenantId), identityType, identityValue);
    }

    public UserLoginCredentialDTO getUserLoginCredential(TenantId tenantId, String identityType, String identityValue) {
        UserIdentity userIdentity = userRepository.findUserIdentity(tenantId, toIdentityType(identityType), identityValue)
                .orElseThrow(() -> new IllegalArgumentException("User identity not found"));
        UserCredential passwordCredential = userRepository.findUserCredential(tenantId, userIdentity.getUserId(), UserCredentialType.PASSWORD)
                .orElseThrow(() -> new IllegalArgumentException("Password credential not found"));
        User user = userRepository.findUserById(userIdentity.getTenantId(), userIdentity.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userIdentity.getUserId()));
        return new UserLoginCredentialDTO(user.getTenantId().value(), user.getId().value(),
                user.getAccount(), user.getPhone(),
                userIdentity.getIdentityType().value(), userIdentity.getIdentityValue(), userIdentity.isEnabled(),
                passwordCredential.getId() == null ? null : passwordCredential.getId().value(),
                passwordCredential.getCredentialType().value(), passwordCredential.getStatus().value(),
                passwordCredential.isNeedChangePassword(), passwordCredential.getExpiresAt(),
                passwordCredential.getLockedUntil(), false, List.of(), user.getStatus().value(),
                passwordCredential.getCredentialValue());
    }

    public UserLoginCredentialDTO getUserLoginCredential(String tenantId, String identityType, String identityValue) {
        return getUserLoginCredential(requireExistingTenantId(tenantId), identityType, identityValue);
    }

    public TenantDTO getTenantByTenantId(String tenantId) {
        Tenant tenant = tenantRepository.findTenantByTenantId(TenantId.of(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return new TenantDTO(tenant.getId(), tenant.getName(), tenant.getTenantCode().value(),
                tenant.getStatus().value(), tenant.getExpiredAt());
    }

    public UserPageResultDTO pageUsers(UserPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        String tenantIdValue = query.getTenantId().value();
        return new UserPageResultDTO(userRepository.pageUsers(query.getTenantId(), query.getAccount(), query.getName(),
                query.getPhone(), query.getStatus(), pageNo, pageSize).stream()
                .map(user -> toSummaryDto(user, tenantIdValue))
                .toList(),
                userRepository.countUsers(query.getTenantId(), query.getAccount(), query.getName(), query.getPhone(),
                        query.getStatus()),
                pageNo, pageSize);
    }

    public UserDTO createUser(TenantId tenantId, String account, String name, String phone, String departmentId) {
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(tenantId, account, null);
        User savedUser = userRepository.save(new User(null, tenantId, normalize(account), normalize(name), normalize(phone),
                null, toDepartmentId(departmentId), UserStatus.ENABLED));
        return toDetailedDto(savedUser);
    }

    public UserDTO updateUser(TenantId tenantId, String userId, String account, String name, String phone, String departmentId) {
        UserId domainUserId = UserId.of(userId);
        User currentUser = requireUser(tenantId, domainUserId);
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(tenantId, account, domainUserId);
        User savedUser = userRepository.save(new User(
                currentUser.getId(),
                tenantId,
                normalize(account),
                normalize(name),
                currentUser.getAvatarObjectId(),
                normalize(phone),
                currentUser.getPasswordHash(),
                toDepartmentId(departmentId),
                currentUser.getStatus(),
                currentUser.getCreatedBy(),
                currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt()));
        return toDetailedDto(savedUser);
    }

    public UserDTO updateUserStatus(TenantId tenantId, String userId, UpmsStatusEnum status) {
        UserId domainUserId = UserId.of(userId);
        User currentUser = requireUser(tenantId, domainUserId);
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
            sessionCommandFacade.invalidateUserSessions(tenantId.value(), userId, "USER_DISABLED");
        }
        return toDetailedDto(savedUser);
    }

    public Optional<String> getAvatarAccessUrl(TenantId tenantId, String userId) {
        User user = requireUser(tenantId, UserId.of(userId));
        if (user.getAvatarObjectId() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(resolveAvatarUrl(user.getAvatarObjectId()));
    }

    public void deleteUser(TenantId tenantId, String userId) {
        UserId domainUserId = UserId.of(userId);
        User currentUser = requireUser(tenantId, domainUserId);
        userRepository.deleteUser(tenantId, domainUserId);
        if (currentUser.getAvatarObjectId() != null) {
            storedObjectFacade.clearObjectReference(currentUser.getAvatarObjectId(), USER_AVATAR_OWNER_TYPE,
                    userId);
        }
        sessionCommandFacade.invalidateUserSessions(tenantId.value(), userId, "USER_DELETED");
    }

    public UserDTO initPassword(TenantId tenantId, String userId) {
        UserId domainUserId = UserId.of(userId);
        requireUser(tenantId, domainUserId);
        User user = userRepository.updatePassword(tenantId, domainUserId, DEFAULT_PASSWORD, true);
        sessionCommandFacade.invalidateUserSessions(tenantId.value(), userId, "USER_PASSWORD_INITIALIZED");
        return toDetailedDto(user);
    }

    public UserDTO resetPassword(TenantId tenantId, String userId, String newPassword) {
        UserId domainUserId = UserId.of(userId);
        requireUser(tenantId, domainUserId);
        validateRequired(newPassword, "newPassword");
        User user = userRepository.updatePassword(tenantId, domainUserId, normalize(newPassword), true);
        sessionCommandFacade.invalidateUserSessions(tenantId.value(), userId, "USER_PASSWORD_RESET");
        return toDetailedDto(user);
    }

    public void changePassword(TenantId tenantId, UserId userId, String oldPassword, String newPassword) {
        User user = requireUser(tenantId, userId);
        UserCredential passwordCredential = userRepository.findUserCredential(tenantId, userId, UserCredentialType.PASSWORD)
                .orElseThrow(() -> new IllegalArgumentException("Password credential not found: " + userId));
        validateRequired(oldPassword, "oldPassword");
        validateRequired(newPassword, "newPassword");
        if (!passwordEncoder.matches(oldPassword, passwordCredential.getCredentialValue())) {
            throw new IllegalArgumentException("Old password invalid");
        }
        userRepository.updatePassword(tenantId, userId, normalize(newPassword), false);
    }

    public void changePassword(String tenantId, String userId, String oldPassword, String newPassword) {
        changePassword(requireExistingTenantId(tenantId), UserId.of(userId), oldPassword, newPassword);
    }

    public List<RoleDTO> assignRoles(TenantId tenantId, String userId, List<String> roleIds) {
        UserId domainUserId = UserId.of(userId);
        requireUser(tenantId, domainUserId);
        String tenantIdValue = tenantId.value();
        List<RoleId> domainRoleIds = roleIds == null ? List.of() : roleIds.stream().map(RoleId::of).toList();
        return userRepository.assignRoles(tenantId, domainUserId, domainRoleIds).stream()
                .map(role -> toRoleDto(role, tenantIdValue))
                .toList();
    }

    public List<RoleDTO> getRolesByUserId(TenantId tenantId, UserId userId) {
        requireUser(tenantId, userId);
        String tenantIdValue = tenantId.value();
        return roleRepository.findRolesByUserId(tenantId, userId).stream()
                .map(role -> toRoleDto(role, tenantIdValue))
                .toList();
    }

    public List<RoleDTO> getRolesByUserId(TenantId tenantId, String userId) {
        return getRolesByUserId(tenantId, UserId.of(userId));
    }

    public List<UserDTO> importUsers(TenantId tenantId, List<UserImportCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        return commands.stream()
                .map(command -> createUser(tenantId, command.account(), command.name(), command.phone(),
                        command.departmentId()))
                .toList();
    }

    public List<UserDTO> exportUsers(UserPageQueryDTO query) {
        String tenantIdValue = query.getTenantId().value();
        return userRepository.listUsers(query.getTenantId(), query.getAccount(), query.getName(), query.getPhone(),
                query.getStatus()).stream().map(user -> toSummaryDto(user, tenantIdValue)).toList();
    }

    public UserDTO updateAvatar(TenantId tenantId, String userId, String originalFilename, String contentType, Long size,
                                InputStream inputStream) {
        User currentUser = requireUser(tenantId, UserId.of(userId));
        AvatarImage avatarImage = readAndValidateAvatar(originalFilename, contentType, size, inputStream);
        StoredObjectDTO storedObject = uploadAvatarObject(tenantId, avatarImage);
        storedObjectFacade.markObjectReferenced(storedObject.getId(), USER_AVATAR_OWNER_TYPE, userId);
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
                        userId);
            }
            return toDto(savedUser, storedObject.getAccessEndpoint(), savedUser.getTenantId().value());
        } catch (RuntimeException ex) {
            storedObjectFacade.clearObjectReference(storedObject.getId(), USER_AVATAR_OWNER_TYPE, userId);
            throw ex;
        }
    }

    private User requireUser(TenantId tenantId, UserId userId) {
        return userRepository.findUserById(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private TenantId requireExistingTenantId(String tenantId) {
        validateRequired(tenantId, "tenantId");
        return tenantRepository.findTenantByTenantId(TenantId.of(tenantId))
                .map(Tenant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    private void ensureAccountUnique(TenantId tenantId, String account, UserId excludedUserId) {
        userRepository.findUserByAccount(tenantId, normalize(account))
                .filter(existingUser -> !existingUser.getId().equals(excludedUserId))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("User account already exists: " + account);
                });
    }

    private UserDTO toDetailedDto(User user) {
        return toDto(user, resolveAvatarUrl(user.getAvatarObjectId()), user.getTenantId().value());
    }

    private UserDTO toSummaryDto(User user, String tenantIdValue) {
        return toDto(user, null, tenantIdValue);
    }

    private UserDTO toDto(User user, String avatarUrl, String tenantIdValue) {
        return new UserDTO(user.getId().value(), tenantIdValue, user.getAccount(), user.getName(),
                user.getAvatarObjectId(), user.getPhone(),
                user.getDepartmentId() == null ? null : user.getDepartmentId().value(), avatarUrl, user.getStatus().value());
    }

    private DepartmentId toDepartmentId(String departmentId) {
        return departmentId == null || departmentId.isBlank() ? null : DepartmentId.of(departmentId.trim());
    }

    private RoleDTO toRoleDto(Role role, String tenantIdValue) {
        return new RoleDTO(role.getId() == null ? null : role.getId().value(), tenantIdValue, role.getCode(), role.getName(),
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

    private StoredObjectDTO uploadAvatarObject(TenantId tenantId, AvatarImage avatarImage) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(avatarImage.bytes())) {
            return storedObjectFacade.uploadObject(new UploadObjectCommand(
                    USER_AVATAR_OWNER_TYPE,
                    tenantId.value(),
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

    private UserIdentityType toIdentityType(String identityType) {
        validateRequired(identityType, "identityType");
        return UserIdentityType.fromValue(normalize(identityType).toUpperCase(Locale.ROOT));
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
