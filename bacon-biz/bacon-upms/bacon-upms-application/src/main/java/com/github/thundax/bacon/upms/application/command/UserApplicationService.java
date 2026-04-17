package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.application.assembler.RoleAssembler;
import com.github.thundax.bacon.upms.application.assembler.TenantAssembler;
import com.github.thundax.bacon.upms.application.assembler.UserAssembler;
import com.github.thundax.bacon.upms.application.assembler.UserIdentityAssembler;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
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
        User user = requireUser(userId);
        return UserAssembler.toDto(
                user,
                resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                resolveAvatarUrl(user.getAvatarObjectId()));
    }

    public UserIdentityDTO getUserIdentity(UserIdentityType identityType, String identityValue) {
        UserIdentity userIdentity = userRepository
                .findUserIdentity(identityType, identityValue)
                .orElseThrow(() -> new NotFoundException("User identity not found"));
        return UserIdentityAssembler.toDto(userIdentity);
    }

    public UserLoginCredentialDTO getUserLoginCredential(UserIdentityType identityType, String identityValue) {
        UserIdentity userIdentity = userRepository
                .findUserIdentity(identityType, identityValue)
                .orElseThrow(() -> new NotFoundException("User identity not found"));
        UserCredential passwordCredential = userRepository
                .findUserCredential(userIdentity.getUserId(), UserCredentialType.PASSWORD)
                .orElseThrow(() -> new NotFoundException("Password credential not found"));
        User user = userRepository
                .findUserById(userIdentity.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + userIdentity.getUserId()));
        String account = resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT);
        String phone = resolveIdentityValue(user.getId(), UserIdentityType.PHONE);
        return UserIdentityAssembler.toLoginCredentialDto(user, userIdentity, passwordCredential, account, phone);
    }

    public TenantDTO getTenantByTenantId(TenantId tenantId) {
        Tenant tenant = tenantRepository
                .findTenantById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantId.value()));
        return TenantAssembler.toDto(tenant);
    }

    public PageResultDTO<UserDTO> pageUsers(
            String account, String name, String phone, UserStatus status, Integer pageNo, Integer pageSize) {
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        return new PageResultDTO<>(
                userRepository.pageUsers(account, name, phone, status, normalizedPageNo, normalizedPageSize).stream()
                        .map(user -> UserAssembler.toDto(
                                user,
                                resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                                resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                                null))
                        .toList(),
                userRepository.countUsers(account, name, phone, status),
                normalizedPageNo,
                normalizedPageSize);
    }

    @Transactional
    public UserDTO createUser(String account, String name, String phone, DepartmentId departmentId) {
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(account, null);
        String normalizedAccount = account.trim();
        String normalizedPhone = phone == null ? null : phone.trim();
        User savedUser = userRepository.insert(
                User.create(ids.userId(), name.trim(), null, departmentId, UserStatus.ENABLED),
                normalizedAccount,
                normalizedPhone,
                UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                normalizedPhone == null ? null : UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
        return UserAssembler.toDto(
                savedUser,
                resolveIdentityValue(savedUser.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(savedUser.getId(), UserIdentityType.PHONE),
                resolveAvatarUrl(savedUser.getAvatarObjectId()));
    }

    @Transactional
    public UserDTO updateUser(UserId userId, String account, String name, String phone, DepartmentId departmentId) {
        User currentUser = requireUser(userId);
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(account, userId);
        String normalizedAccount = account.trim();
        String normalizedPhone = phone == null ? null : phone.trim();
        User savedUser = userRepository.update(
                currentUser.update(name.trim(), currentUser.getAvatarObjectId(), departmentId, currentUser.getStatus()),
                normalizedAccount,
                normalizedPhone,
                UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                normalizedPhone == null ? null : UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
        return UserAssembler.toDto(
                savedUser,
                resolveIdentityValue(savedUser.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(savedUser.getId(), UserIdentityType.PHONE),
                resolveAvatarUrl(savedUser.getAvatarObjectId()));
    }

    @Transactional
    public UserDTO updateUserStatus(UserId userId, UserStatus status) {
        User currentUser = requireUser(userId);
        if (status == null) {
            throw new BadRequestException("status must not be null");
        }
        User savedUser = userRepository.update(
                currentUser.update(
                        currentUser.getName(), currentUser.getAvatarObjectId(), currentUser.getDepartmentId(), status),
                requireIdentityValue(currentUser.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(currentUser.getId(), UserIdentityType.PHONE),
                UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                resolveIdentityValue(currentUser.getId(), UserIdentityType.PHONE) == null
                        ? null
                        : UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
        if (UserStatus.DISABLED == savedUser.getStatus()) {
            sessionCommandFacade.invalidateUserSessions(
                    new SessionInvalidateUserFacadeRequest(
                            BaconContextHolder.requireTenantId(), userId.value(), "USER_DISABLED"));
        }
        return UserAssembler.toDto(
                savedUser,
                resolveIdentityValue(savedUser.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(savedUser.getId(), UserIdentityType.PHONE),
                resolveAvatarUrl(savedUser.getAvatarObjectId()));
    }

    public Optional<String> getAvatarAccessUrl(UserId userId) {
        User user = requireUser(userId);
        if (user.getAvatarObjectId() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(resolveAvatarUrl(user.getAvatarObjectId()));
    }

    @Transactional
    public void deleteUser(UserId userId) {
        User currentUser = requireUser(userId);
        userRepository.deleteUser(userId);
        if (currentUser.getAvatarObjectId() != null) {
            storedObjectFacade.clearObjectReference(
                    currentUser.getAvatarObjectId().externalValue(),
                    USER_AVATAR_OWNER_TYPE,
                    String.valueOf(userId.value()));
        }
        sessionCommandFacade.invalidateUserSessions(
                new SessionInvalidateUserFacadeRequest(
                        BaconContextHolder.requireTenantId(), userId.value(), "USER_DELETED"));
    }

    @Transactional
    public UserDTO initPassword(UserId userId) {
        requireUser(userId);
        User user = userRepository.updatePassword(
                userId, DEFAULT_PASSWORD, true, UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
        sessionCommandFacade.invalidateUserSessions(
                new SessionInvalidateUserFacadeRequest(
                        BaconContextHolder.requireTenantId(), userId.value(), "USER_PASSWORD_INITIALIZED"));
        return UserAssembler.toDto(
                user,
                resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                resolveAvatarUrl(user.getAvatarObjectId()));
    }

    @Transactional
    public UserDTO resetPassword(UserId userId, String newPassword) {
        requireUser(userId);
        validateRequired(newPassword, "newPassword");
        User user = userRepository.updatePassword(
                userId, newPassword.trim(), true, UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
        sessionCommandFacade.invalidateUserSessions(
                new SessionInvalidateUserFacadeRequest(
                        BaconContextHolder.requireTenantId(), userId.value(), "USER_PASSWORD_RESET"));
        return UserAssembler.toDto(
                user,
                resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                resolveAvatarUrl(user.getAvatarObjectId()));
    }

    @Transactional
    public void changePassword(UserId userId, String oldPassword, String newPassword) {
        requireUser(userId);
        UserCredential passwordCredential = userRepository
                .findUserCredential(userId, UserCredentialType.PASSWORD)
                .orElseThrow(() -> new NotFoundException("Password credential not found: " + userId));
        validateRequired(oldPassword, "oldPassword");
        validateRequired(newPassword, "newPassword");
        if (!passwordEncoder.matches(oldPassword, passwordCredential.getCredentialValue())) {
            throw new BadRequestException("Old password invalid");
        }
        userRepository.updatePassword(
                userId, newPassword.trim(), false, UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
    }

    @Transactional
    public List<RoleDTO> assignRoles(UserId userId, List<RoleId> roleIds) {
        requireUser(userId);
        List<RoleId> domainRoleIds = roleIds == null ? List.of() : roleIds;
        return userRepository.assignRoles(userId, domainRoleIds).stream()
                .map(RoleAssembler::toDto)
                .toList();
    }

    public List<RoleDTO> getRolesByUserId(UserId userId) {
        requireUser(userId);
        return roleRepository.findRolesByUserId(userId).stream()
                .map(RoleAssembler::toDto)
                .toList();
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

    public List<UserDTO> exportUsers(String account, String name, String phone, UserStatus status) {
        return userRepository.listUsers(account, name, phone, status).stream()
                .map(user -> UserAssembler.toDto(
                        user,
                        resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                        resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                        null))
                .toList();
    }

    public UserDTO updateAvatar(
            UserId userId, String originalFilename, String contentType, Long size, InputStream inputStream) {
        User currentUser = requireUser(userId);
        AvatarImage avatarImage = readAndValidateAvatar(originalFilename, contentType, size, inputStream);
        StoredObjectDTO storedObject = uploadAvatarObject(avatarImage);
        StoredObjectId storedObjectId = storedObject.getId();
        storedObjectFacade.markObjectReferenced(
                storedObjectId.externalValue(), USER_AVATAR_OWNER_TYPE, String.valueOf(userId.value()));
        StoredObjectId previousAvatarObjectId = currentUser.getAvatarObjectId();
        try {
            User savedUser = userRepository.update(
                    currentUser.update(
                            currentUser.getName(),
                            storedObjectId,
                            currentUser.getDepartmentId(),
                            currentUser.getStatus()),
                    requireIdentityValue(currentUser.getId(), UserIdentityType.ACCOUNT),
                    resolveIdentityValue(currentUser.getId(), UserIdentityType.PHONE),
                    UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                    resolveIdentityValue(currentUser.getId(), UserIdentityType.PHONE) == null
                            ? null
                            : UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                    UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
            if (previousAvatarObjectId != null && !previousAvatarObjectId.equals(storedObjectId)) {
                storedObjectFacade.clearObjectReference(
                        previousAvatarObjectId.externalValue(), USER_AVATAR_OWNER_TYPE, String.valueOf(userId.value()));
            }
            return UserAssembler.toDto(
                    savedUser,
                    resolveIdentityValue(savedUser.getId(), UserIdentityType.ACCOUNT),
                    resolveIdentityValue(savedUser.getId(), UserIdentityType.PHONE),
                    storedObject.getAccessEndpoint());
        } catch (RuntimeException ex) {
            storedObjectFacade.clearObjectReference(
                    storedObjectId.externalValue(), USER_AVATAR_OWNER_TYPE, String.valueOf(userId.value()));
            throw ex;
        }
    }

    private User requireUser(UserId userId) {
        return userRepository
                .findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private void ensureAccountUnique(String account, UserId excludedUserId) {
        userRepository
                .findUserByAccount(account == null ? null : account.trim())
                .filter(existingUser -> !existingUser.getId().equals(excludedUserId))
                .ifPresent(existingUser -> {
                    throw new ConflictException("User account already exists: " + account);
                });
    }

    private String requireIdentityValue(UserId userId, UserIdentityType identityType) {
        return userRepository
                .findUserIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElseThrow(() -> new NotFoundException(
                        "User identity not found: " + userId + "/" + identityType.value()));
    }

    private String resolveIdentityValue(UserId userId, UserIdentityType identityType) {
        return userRepository
                .findUserIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElse(null);
    }

    private DepartmentId resolveDepartmentIdByCode(String departmentCode) {
        if (departmentCode == null || departmentCode.isBlank()) {
            return null;
        }
        return departmentRepository
                .findDepartmentByCode(DepartmentCode.of(departmentCode))
                .map(Department::getId)
                .orElseThrow(() -> new NotFoundException("Department not found by code: " + departmentCode));
    }

    private AvatarImage readAndValidateAvatar(
            String originalFilename, String contentType, Long size, InputStream inputStream) {
        validateRequired(originalFilename, "originalFilename");
        if (inputStream == null) {
            throw new BadRequestException("avatar file must not be null");
        }
        if (size == null || size <= 0L) {
            throw new BadRequestException("avatar size must be greater than 0");
        }
        if (size > MAX_AVATAR_SIZE) {
            throw new BadRequestException("avatar size exceeds 2MB");
        }
        String normalizedContentType =
                contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_AVATAR_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BadRequestException("avatar contentType must be image/jpeg or image/png");
        }
        try {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                throw new BadRequestException("avatar file must not be empty");
            }
            if (bytes.length > MAX_AVATAR_SIZE) {
                throw new BadRequestException("avatar size exceeds 2MB");
            }
            String actualContentType = detectAvatarContentType(bytes);
            if (!normalizedContentType.equals(actualContentType)) {
                throw new BadRequestException("avatar contentType does not match image data");
            }
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
            if (bufferedImage == null) {
                throw new BadRequestException("avatar file is not a valid image");
            }
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            if (width != height) {
                throw new BadRequestException("avatar image must be square");
            }
            if (width < MIN_AVATAR_PIXEL || width > MAX_AVATAR_PIXEL) {
                throw new BadRequestException("avatar image width must be between 128 and 1024");
            }
            if (height < MIN_AVATAR_PIXEL || height > MAX_AVATAR_PIXEL) {
                throw new BadRequestException("avatar image height must be between 128 and 1024");
            }
            return new AvatarImage(originalFilename.trim(), actualContentType, (long) bytes.length, bytes);
        } catch (IOException ex) {
            throw new BadRequestException("avatar file cannot be read", ex);
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
                throw new BadRequestException("avatar file is not a valid image");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                throw new BadRequestException("avatar file is not a supported image");
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
                throw new BadRequestException("avatar image format must be jpeg or png");
            } finally {
                reader.dispose();
            }
        }
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
    }

    private record AvatarImage(String originalFilename, String contentType, Long size, byte[] bytes) {}
}
