package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.codec.RoleIdCodec;
import com.github.thundax.bacon.upms.application.codec.TenantCodeCodec;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.dto.UserPageQueryDTO;
import com.github.thundax.bacon.upms.api.enums.EnableStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserApplicationService {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final String USER_AVATAR_OWNER_TYPE = "UPMS_USER_AVATAR";
    private static final String USER_AVATAR_CATEGORY = "avatar";
    private static final long MAX_AVATAR_SIZE = 2L * 1024L * 1024L;
    private static final int MIN_AVATAR_PIXEL = 128;
    private static final int MAX_AVATAR_PIXEL = 1024;
    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final String USER_IDENTITY_ID_BIZ_TAG = "user-identity-id";
    private static final String USER_CREDENTIAL_ID_BIZ_TAG = "user-credential-id";

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final SessionCommandFacade sessionCommandFacade;
    private final PasswordEncoder passwordEncoder;
    private final StoredObjectFacade storedObjectFacade;
    private final Ids ids;
    private final IdGenerator idGenerator;

    public UserApplicationService(
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            SessionCommandFacade sessionCommandFacade,
            PasswordEncoder passwordEncoder,
            StoredObjectFacade storedObjectFacade,
            Ids ids,
            IdGenerator idGenerator) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.sessionCommandFacade = sessionCommandFacade;
        this.passwordEncoder = passwordEncoder;
        this.storedObjectFacade = storedObjectFacade;
        this.ids = ids;
        this.idGenerator = idGenerator;
    }

    public UserDTO getUserById(UserId userId) {
        return toDetailedDto(requireUser(userId));
    }

    public UserDTO getUserById(Long userId) {
        return getUserById(UserIdCodec.toDomain(userId));
    }

    public UserIdentityDTO getUserIdentity(String identityType, String identityValue) {
        UserIdentity userIdentity = userRepository
                .findUserIdentity(toIdentityType(identityType), identityValue)
                .orElseThrow(() -> new IllegalArgumentException("User identity not found"));
        return new UserIdentityDTO(
                userIdentity.getId() == null ? null : userIdentity.getId().value(),
                userIdentity.getUserId().value(),
                userIdentity.getIdentityType().value(),
                userIdentity.getIdentityValue(),
                userIdentity.getStatus() == null
                        ? null
                        : userIdentity.getStatus().value());
    }

    public UserLoginCredentialDTO getUserLoginCredential(String identityType, String identityValue) {
        UserIdentity userIdentity = userRepository
                .findUserIdentity(toIdentityType(identityType), identityValue)
                .orElseThrow(() -> new IllegalArgumentException("User identity not found"));
        UserCredential passwordCredential = userRepository
                .findUserCredential(userIdentity.getUserId(), UserCredentialType.PASSWORD)
                .orElseThrow(() -> new IllegalArgumentException("Password credential not found"));
        User user = userRepository
                .findUserById(userIdentity.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userIdentity.getUserId()));
        String account = resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT);
        String phone = resolveIdentityValue(user.getId(), UserIdentityType.PHONE);
        return new UserLoginCredentialDTO(
                user.getId().value(),
                userIdentity.getId() == null ? null : userIdentity.getId().value(),
                account,
                phone,
                userIdentity.getIdentityType().value(),
                userIdentity.getIdentityValue(),
                userIdentity.getStatus() == null
                        ? null
                        : userIdentity.getStatus().value(),
                passwordCredential.getId() == null
                        ? null
                        : passwordCredential.getId().value(),
                passwordCredential.getCredentialType().value(),
                passwordCredential.getStatus().value(),
                passwordCredential.isNeedChangePassword(),
                passwordCredential.getExpiresAt(),
                passwordCredential.getLockedUntil(),
                false,
                List.of(),
                user.getStatus().value(),
                passwordCredential.getCredentialValue());
    }

    public TenantDTO getTenantByTenantId(Long tenantId) {
        Tenant tenant = tenantRepository
                .findTenantByTenantId(TenantId.of(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return new TenantDTO(
                tenant.getId(),
                tenant.getName(),
                TenantCodeCodec.toValue(tenant.getTenantCode()),
                tenant.getStatus().value(),
                tenant.getExpiredAt());
    }

    public PageResultDTO<UserDTO> pageUsers(UserPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        return new PageResultDTO<>(
                userRepository
                        .pageUsers(
                                query.getAccount(),
                                query.getName(),
                                query.getPhone(),
                                query.getStatus(),
                                pageNo,
                                pageSize)
                        .stream()
                        .map(this::toSummaryDto)
                        .toList(),
                userRepository.countUsers(query.getAccount(), query.getName(), query.getPhone(), query.getStatus()),
                pageNo,
                pageSize);
    }

    @Transactional
    public UserDTO createUser(String account, String name, String phone, Long departmentId) {
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(account, null);
        String normalizedAccount = normalize(account);
        String normalizedPhone = normalize(phone);
        DepartmentId domainDepartmentId = toDepartmentId(departmentId);
        User savedUser = userRepository.save(
                User.create(ids.userId(), normalize(name), null, domainDepartmentId, UserStatus.ENABLED),
                normalizedAccount,
                normalizedPhone,
                nextUserIdentityId(),
                normalizedPhone == null ? null : nextUserIdentityId(),
                nextUserCredentialId());
        return toDetailedDto(savedUser);
    }

    @Transactional
    public UserDTO updateUser(Long userId, String account, String name, String phone, Long departmentId) {
        UserId domainUserId = UserIdCodec.toDomain(userId);
        User currentUser = requireUser(domainUserId);
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(account, domainUserId);
        String normalizedAccount = normalize(account);
        String normalizedPhone = normalize(phone);
        User savedUser = userRepository.save(
                currentUser.update(
                        normalize(name),
                        currentUser.getAvatarObjectId(),
                        toDepartmentId(departmentId),
                        currentUser.getStatus()),
                normalizedAccount,
                normalizedPhone,
                nextUserIdentityId(),
                normalizedPhone == null ? null : nextUserIdentityId(),
                nextUserCredentialId());
        return toDetailedDto(savedUser);
    }

    @Transactional
    public UserDTO updateUserStatus(Long userId, EnableStatusEnum status) {
        UserId domainUserId = UserIdCodec.toDomain(userId);
        User currentUser = requireUser(domainUserId);
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        User savedUser = userRepository.save(
                currentUser.update(
                        currentUser.getName(),
                        currentUser.getAvatarObjectId(),
                        currentUser.getDepartmentId(),
                        toDomainStatus(status)),
                requireIdentityValue(currentUser.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(currentUser.getId(), UserIdentityType.PHONE),
                nextUserIdentityId(),
                resolveIdentityValue(currentUser.getId(), UserIdentityType.PHONE) == null ? null : nextUserIdentityId(),
                nextUserCredentialId());
        if (UserStatus.DISABLED == savedUser.getStatus()) {
            sessionCommandFacade.invalidateUserSessions(BaconContextHolder.requireTenantId(), domainUserId.value(), "USER_DISABLED");
        }
        return toDetailedDto(savedUser);
    }

    public Optional<String> getAvatarAccessUrl(Long userId) {
        User user = requireUser(UserIdCodec.toDomain(userId));
        if (user.getAvatarObjectId() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(resolveAvatarUrl(user.getAvatarObjectId()));
    }

    @Transactional
    public void deleteUser(Long userId) {
        UserId domainUserId = UserIdCodec.toDomain(userId);
        User currentUser = requireUser(domainUserId);
        userRepository.deleteUser(domainUserId);
        if (currentUser.getAvatarObjectId() != null) {
            storedObjectFacade.clearObjectReference(
                    currentUser.getAvatarObjectId().externalValue(), USER_AVATAR_OWNER_TYPE, String.valueOf(userId));
        }
        sessionCommandFacade.invalidateUserSessions(BaconContextHolder.requireTenantId(), domainUserId.value(), "USER_DELETED");
    }

    @Transactional
    public UserDTO initPassword(Long userId) {
        UserId domainUserId = UserIdCodec.toDomain(userId);
        requireUser(domainUserId);
        User user = userRepository.updatePassword(domainUserId, DEFAULT_PASSWORD, true, nextUserCredentialId());
        sessionCommandFacade.invalidateUserSessions(
                BaconContextHolder.requireTenantId(), domainUserId.value(), "USER_PASSWORD_INITIALIZED");
        return toDetailedDto(user);
    }

    @Transactional
    public UserDTO resetPassword(Long userId, String newPassword) {
        UserId domainUserId = UserIdCodec.toDomain(userId);
        requireUser(domainUserId);
        validateRequired(newPassword, "newPassword");
        User user = userRepository.updatePassword(domainUserId, normalize(newPassword), true, nextUserCredentialId());
        sessionCommandFacade.invalidateUserSessions(
                BaconContextHolder.requireTenantId(), domainUserId.value(), "USER_PASSWORD_RESET");
        return toDetailedDto(user);
    }

    @Transactional
    public void changePassword(UserId userId, String oldPassword, String newPassword) {
        requireUser(userId);
        UserCredential passwordCredential = userRepository
                .findUserCredential(userId, UserCredentialType.PASSWORD)
                .orElseThrow(() -> new IllegalArgumentException("Password credential not found: " + userId));
        validateRequired(oldPassword, "oldPassword");
        validateRequired(newPassword, "newPassword");
        if (!passwordEncoder.matches(oldPassword, passwordCredential.getCredentialValue())) {
            throw new IllegalArgumentException("Old password invalid");
        }
        userRepository.updatePassword(userId, normalize(newPassword), false, nextUserCredentialId());
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        changePassword(UserIdCodec.toDomain(userId), oldPassword, newPassword);
    }

    @Transactional
    public List<RoleDTO> assignRoles(Long userId, List<Long> roleIds) {
        UserId domainUserId = UserIdCodec.toDomain(userId);
        requireUser(domainUserId);
        List<RoleId> domainRoleIds =
                roleIds == null ? List.of() : roleIds.stream().map(RoleIdCodec::toDomain).toList();
        return userRepository.assignRoles(domainUserId, domainRoleIds).stream()
                .map(this::toRoleDto)
                .toList();
    }

    public List<RoleDTO> getRolesByUserId(UserId userId) {
        requireUser(userId);
        return roleRepository.findRolesByUserId(userId).stream()
                .map(this::toRoleDto)
                .toList();
    }

    public List<RoleDTO> getRolesByUserId(Long userId) {
        return getRolesByUserId(UserIdCodec.toDomain(userId));
    }

    @Transactional
    public List<UserDTO> importUsers(List<UserImportCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        return commands.stream()
                .map(command -> createUser(
                        command.account(),
                        command.name(),
                        command.phone(),
                        resolveDepartmentIdByCode(command.departmentCode())))
                .toList();
    }

    public List<UserDTO> exportUsers(UserPageQueryDTO query) {
        return userRepository
                .listUsers(query.getAccount(), query.getName(), query.getPhone(), query.getStatus())
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public UserDTO updateAvatar(
            Long userId,
            String originalFilename,
            String contentType,
            Long size,
            InputStream inputStream) {
        User currentUser = requireUser(UserIdCodec.toDomain(userId));
        AvatarImage avatarImage = readAndValidateAvatar(originalFilename, contentType, size, inputStream);
        StoredObjectDTO storedObject = uploadAvatarObject(avatarImage);
        StoredObjectId storedObjectId = storedObject.getId();
        storedObjectFacade.markObjectReferenced(
                storedObjectId.externalValue(), USER_AVATAR_OWNER_TYPE, String.valueOf(userId));
        StoredObjectId previousAvatarObjectId = currentUser.getAvatarObjectId();
        try {
            User savedUser = userRepository.save(
                    currentUser.update(
                            currentUser.getName(),
                            storedObjectId,
                            currentUser.getDepartmentId(),
                            currentUser.getStatus()),
                    requireIdentityValue(currentUser.getId(), UserIdentityType.ACCOUNT),
                    resolveIdentityValue(currentUser.getId(), UserIdentityType.PHONE),
                    nextUserIdentityId(),
                    resolveIdentityValue(currentUser.getId(), UserIdentityType.PHONE) == null ? null : nextUserIdentityId(),
                    nextUserCredentialId());
            if (previousAvatarObjectId != null && !previousAvatarObjectId.equals(storedObjectId)) {
                storedObjectFacade.clearObjectReference(
                        previousAvatarObjectId.externalValue(), USER_AVATAR_OWNER_TYPE, String.valueOf(userId));
            }
            return toDto(savedUser, storedObject.getAccessEndpoint());
        } catch (RuntimeException ex) {
            storedObjectFacade.clearObjectReference(
                    storedObjectId.externalValue(), USER_AVATAR_OWNER_TYPE, String.valueOf(userId));
            throw ex;
        }
    }

    private User requireUser(UserId userId) {
        return userRepository
                .findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private void ensureAccountUnique(String account, UserId excludedUserId) {
        userRepository
                .findUserByAccount(normalize(account))
                .filter(existingUser -> !existingUser.getId().equals(excludedUserId))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("User account already exists: " + account);
                });
    }

    private UserDTO toDetailedDto(User user) {
        return toDto(user, resolveAvatarUrl(user.getAvatarObjectId()));
    }

    private UserDTO toSummaryDto(User user) {
        return toDto(user, null);
    }

    private UserDTO toDto(User user, String avatarUrl) {
        return new UserDTO(
                user.getId().value(),
                resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                user.getName(),
                user.getAvatarObjectId() == null
                        ? null
                        : user.getAvatarObjectId().value(),
                resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                DepartmentIdCodec.toValue(user.getDepartmentId()),
                avatarUrl,
                user.getStatus().value());
    }

    private String requireIdentityValue(UserId userId, UserIdentityType identityType) {
        return userRepository
                .findUserIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User identity not found: " + userId + "/" + identityType.value()));
    }

    private String resolveIdentityValue(UserId userId, UserIdentityType identityType) {
        return userRepository
                .findUserIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElse(null);
    }

    private DepartmentId toDepartmentId(Long departmentId) {
        return DepartmentIdCodec.toDomain(departmentId);
    }

    private Long resolveDepartmentIdByCode(String departmentCode) {
        if (departmentCode == null || departmentCode.isBlank()) {
            return null;
        }
        return departmentRepository
                .findDepartmentByCode(departmentCode.trim())
                .map(department -> DepartmentIdCodec.toValue(department.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Department not found by code: " + departmentCode));
    }

    private RoleDTO toRoleDto(Role role) {
        return new RoleDTO(
                RoleIdCodec.toValue(role.getId()),
                role.getCode(),
                role.getName(),
                role.getRoleType() == null ? null : role.getRoleType().value(),
                role.getDataScopeType() == null ? null : role.getDataScopeType().value(),
                role.getStatus() == null ? null : role.getStatus().value());
    }

    private AvatarImage readAndValidateAvatar(
            String originalFilename, String contentType, Long size, InputStream inputStream) {
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

    private StoredObjectDTO uploadAvatarObject(AvatarImage avatarImage) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(avatarImage.bytes())) {
            return storedObjectFacade.uploadObject(new UploadObjectCommand(
                    USER_AVATAR_OWNER_TYPE,
                    USER_AVATAR_CATEGORY,
                    avatarImage.originalFilename(),
                    avatarImage.contentType(),
                    avatarImage.size(),
                    inputStream));
        } catch (IOException ex) {
            throw new IllegalStateException("failed to close avatar stream", ex);
        }
    }

    private String resolveAvatarUrl(StoredObjectId avatarObjectId) {
        if (avatarObjectId == null) {
            return null;
        }
        StoredObjectDTO storedObject = storedObjectFacade.getObjectById(avatarObjectId.externalValue());
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
        return UserIdentityType.from(normalize(identityType).toUpperCase(Locale.ROOT));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private UserStatus toDomainStatus(EnableStatusEnum status) {
        return UserStatus.valueOf(status.name());
    }

    private UserIdentityId nextUserIdentityId() {
        return UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG));
    }

    private UserCredentialId nextUserCredentialId() {
        return UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG));
    }

    private record AvatarImage(String originalFilename, String contentType, Long size, byte[] bytes) {}
}
