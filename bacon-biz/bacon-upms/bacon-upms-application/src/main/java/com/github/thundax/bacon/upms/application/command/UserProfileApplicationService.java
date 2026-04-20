package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
import com.github.thundax.bacon.storage.api.request.StoredObjectGetFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.upms.application.assembler.RoleAssembler;
import com.github.thundax.bacon.upms.application.assembler.UserAssembler;
import com.github.thundax.bacon.upms.application.codec.DepartmentCodeCodec;
import com.github.thundax.bacon.upms.application.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRoleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileApplicationService {

    private static final String USER_AVATAR_OWNER_TYPE = "UPMS_USER_AVATAR";
    private static final String USER_IDENTITY_ID_BIZ_TAG = "user-identity-id";
    private static final String USER_CREDENTIAL_ID_BIZ_TAG = "user-credential-id";

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final UserRoleRepository userRoleRepository;
    private final SessionCommandFacade sessionCommandFacade;
    private final StoredObjectCommandFacade storedObjectCommandFacade;
    private final StoredObjectReadFacade storedObjectReadFacade;
    private final Ids ids;
    private final IdGenerator idGenerator;

    public UserProfileApplicationService(
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            UserIdentityRepository userIdentityRepository,
            UserRoleRepository userRoleRepository,
            SessionCommandFacade sessionCommandFacade,
            StoredObjectCommandFacade storedObjectCommandFacade,
            StoredObjectReadFacade storedObjectReadFacade,
            Ids ids,
            IdGenerator idGenerator) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.userIdentityRepository = userIdentityRepository;
        this.userRoleRepository = userRoleRepository;
        this.sessionCommandFacade = sessionCommandFacade;
        this.storedObjectCommandFacade = storedObjectCommandFacade;
        this.storedObjectReadFacade = storedObjectReadFacade;
        this.ids = ids;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public UserDTO createUser(String account, String name, String phone, DepartmentId departmentId) {
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(account, null);
        String normalizedAccount = account.trim();
        String normalizedPhone = phone == null ? null : phone.trim();
        User savedUser = userRepository.insert(
                User.create(ids.userId(), name.trim(), null, departmentId),
                normalizedAccount,
                normalizedPhone,
                UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                normalizedPhone == null ? null : UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
        return toUserDto(savedUser);
    }

    @Transactional
    public UserDTO updateUser(UserId userId, String account, String name, String phone, DepartmentId departmentId) {
        User currentUser = requireUser(userId);
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(account, userId);
        String normalizedAccount = account.trim();
        String normalizedPhone = phone == null ? null : phone.trim();
        currentUser.rename(name.trim());
        if (departmentId == null) {
            currentUser.clearDepartment();
        } else {
            currentUser.assignDepartment(departmentId);
        }
        User savedUser = userRepository.update(
                currentUser,
                normalizedAccount,
                normalizedPhone,
                UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                normalizedPhone == null ? null : UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG)),
                UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
        return toUserDto(savedUser);
    }

    @Transactional
    public UserDTO updateUserStatus(UserId userId, UserStatus status) {
        User currentUser = requireUser(userId);
        if (status == null) {
            throw new BadRequestException("status must not be null");
        }
        if (UserStatus.ACTIVE == status) {
            currentUser.activate();
        } else {
            currentUser.disable();
        }
        User savedUser = userRepository.update(
                currentUser,
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
        return toUserDto(savedUser);
    }

    @Transactional
    public void delete(UserId userId) {
        User currentUser = requireUser(userId);
        userRepository.delete(userId);
        if (currentUser.getAvatarStoredObjectNo() != null) {
            storedObjectCommandFacade.clearObjectReference(
                    new StoredObjectReferenceFacadeRequest(
                            currentUser.getAvatarStoredObjectNo().value(),
                            USER_AVATAR_OWNER_TYPE,
                            String.valueOf(userId.value())));
        }
        sessionCommandFacade.invalidateUserSessions(
                new SessionInvalidateUserFacadeRequest(
                        BaconContextHolder.requireTenantId(), userId.value(), "USER_DELETED"));
    }

    @Transactional
    public List<RoleDTO> updateRoleIds(UserId userId, List<RoleId> roleIds) {
        requireUser(userId);
        List<RoleId> domainRoleIds = roleIds == null ? List.of() : roleIds;
        return userRoleRepository.updateRoleIds(userId, domainRoleIds).stream()
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

    private UserDTO toUserDto(User user) {
        return UserAssembler.toDto(
                user,
                resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                resolveAvatarUrl(user.getAvatarStoredObjectNo()));
    }

    private User requireUser(UserId userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private void ensureAccountUnique(String account, UserId excludedUserId) {
        userRepository
                .findByAccount(account == null ? null : account.trim())
                .filter(existingUser -> !existingUser.getId().equals(excludedUserId))
                .ifPresent(existingUser -> {
                    throw new ConflictException("User account already exists: " + account);
                });
    }

    private String requireIdentityValue(UserId userId, UserIdentityType identityType) {
        return userIdentityRepository
                .findIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElseThrow(() -> new NotFoundException(
                        "User identity not found: " + userId + "/" + identityType.value()));
    }

    private String resolveIdentityValue(UserId userId, UserIdentityType identityType) {
        return userIdentityRepository
                .findIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElse(null);
    }

    private DepartmentId resolveDepartmentIdByCode(String departmentCode) {
        if (departmentCode == null || departmentCode.isBlank()) {
            return null;
        }
        return departmentRepository
                .findByCode(DepartmentCodeCodec.toDomain(departmentCode))
                .map(Department::getId)
                .orElseThrow(() -> new NotFoundException("Department not found by code: " + departmentCode));
    }

    private String resolveAvatarUrl(AvatarStoredObjectNo avatarStoredObjectNo) {
        if (avatarStoredObjectNo == null) {
            return null;
        }
        StoredObjectFacadeResponse storedObject =
                storedObjectReadFacade.getObjectByNo(new StoredObjectGetFacadeRequest(avatarStoredObjectNo.value()));
        return storedObject == null ? null : storedObject.getAccessEndpoint();
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
    }
}
