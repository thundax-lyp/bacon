package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class UserRepositoryImpl implements UserRepository {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final UserCredentialType PASSWORD_CREDENTIAL_TYPE = UserCredentialType.PASSWORD;
    private static final UserCredentialFactorLevel PRIMARY_FACTOR_LEVEL = UserCredentialFactorLevel.PRIMARY;
    private static final int PASSWORD_FAILED_LIMIT = 5;
    private static final long PASSWORD_EXPIRE_DAYS = 90L;
    private static final UserIdentityStatus ACTIVE_IDENTITY_STATUS = UserIdentityStatus.ACTIVE;

    private final UserPersistenceSupport support;
    private final RoleRepositoryImpl roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UpmsPermissionCacheSupport cacheSupport;

    public UserRepositoryImpl(
            UserPersistenceSupport support,
            RoleRepositoryImpl roleRepository,
            PasswordEncoder passwordEncoder,
            UpmsPermissionCacheSupport cacheSupport) {
        this.support = support;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Optional<User> findUserById(UserId userId) {
        return support.findUserById(userId);
    }

    @Override
    public Optional<User> findUserByAccount(String account) {
        return support.findUserByAccount(account);
    }

    @Override
    public Optional<UserIdentity> findUserIdentity(UserIdentityType identityType, String identityValue) {
        return support.findUserIdentity(identityType, identityValue);
    }

    @Override
    public Optional<UserIdentity> findUserIdentityByUserId(UserId userId, UserIdentityType identityType) {
        return support.findUserIdentityByUserId(userId, identityType);
    }

    @Override
    public Optional<UserCredential> findUserCredential(UserId userId, UserCredentialType credentialType) {
        return support.findUserCredential(userId, credentialType);
    }

    @Override
    public List<User> pageUsers(
            String account, String name, String phone, UserStatus status, int pageNo, int pageSize) {
        return support.listUsers(account, name, phone, status, pageNo, pageSize);
    }

    @Override
    public long countUsers(String account, String name, String phone, UserStatus status) {
        return support.countUsers(account, name, phone, status);
    }

    @Override
    public List<User> listUsers(String account, String name, String phone, UserStatus status) {
        return support.listUsers(account, name, phone, status, 1, Integer.MAX_VALUE);
    }

    @Override
    public User insert(
            User user,
            String account,
            String phone,
            UserIdentityId accountIdentityId,
            UserIdentityId phoneIdentityId,
            UserCredentialId passwordCredentialIdIfAbsent) {
        return persistUser(
                user, account, phone, accountIdentityId, phoneIdentityId, passwordCredentialIdIfAbsent, true);
    }

    @Override
    public User update(
            User user,
            String account,
            String phone,
            UserIdentityId accountIdentityId,
            UserIdentityId phoneIdentityId,
            UserCredentialId passwordCredentialIdIfAbsent) {
        return persistUser(
                user, account, phone, accountIdentityId, phoneIdentityId, passwordCredentialIdIfAbsent, false);
    }

    private User persistUser(
            User user,
            String account,
            String phone,
            UserIdentityId accountIdentityId,
            UserIdentityId phoneIdentityId,
            UserCredentialId passwordCredentialIdIfAbsent,
            boolean newUser) {
        TenantId tenantId = requireTenantId();
        User savedUser = copyUser(user);
        savedUser = newUser ? support.insertUser(savedUser) : support.updateUser(savedUser);
        UserIdentity accountIdentity = replaceAccountIdentity(tenantId, savedUser, account, accountIdentityId);
        upsertPasswordCredential(
                tenantId,
                savedUser,
                accountIdentity,
                resolvePasswordHash(tenantId, savedUser, newUser),
                newUser,
                false,
                passwordCredentialIdIfAbsent);
        replacePhoneIdentity(tenantId, savedUser, phone, phoneIdentityId);
        return savedUser;
    }

    @Override
    public User updatePassword(
            UserId userId, String password, boolean needChangePassword, UserCredentialId passwordCredentialIdIfAbsent) {
        TenantId tenantId = requireTenantId();
        User currentUser =
                findUserById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        User updatedUser = User.create(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getAvatarObjectId(),
                currentUser.getDepartmentId(),
                currentUser.getStatus());
        User savedUser = support.updateUser(updatedUser);
        UserIdentity accountIdentity = requireUserIdentity(userId, UserIdentityType.ACCOUNT);
        upsertPasswordCredential(
                tenantId,
                savedUser,
                accountIdentity,
                passwordEncoder.encode(password),
                false,
                needChangePassword,
                passwordCredentialIdIfAbsent);
        return savedUser;
    }

    @Override
    public List<Role> assignRoles(UserId userId, List<RoleId> roleIds) {
        TenantId tenantId = requireTenantId();
        List<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository
                        .findRoleById(roleId)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value())))
                .toList();
        roleRepository.bindUserRoles(tenantId, userId, roles);
        cacheSupport.evictUserPermission(tenantId, userId);
        return roles;
    }

    @Override
    public void deleteUser(UserId userId) {
        TenantId tenantId = requireTenantId();
        support.deleteUser(userId);
        roleRepository.clearUserRoles(tenantId, userId);
        support.deleteUserIdentitiesByUser(tenantId, userId);
        support.deleteUserCredentialsByUser(tenantId, userId);
        cacheSupport.evictUserPermission(tenantId, userId);
    }

    private User copyUser(User user) {
        return User.create(
                user.getId(), user.getName(), user.getAvatarObjectId(), user.getDepartmentId(), user.getStatus());
    }

    private UserIdentity replaceAccountIdentity(
            TenantId tenantId, User user, String account, UserIdentityId accountIdentityId) {
        support.deleteUserIdentitiesByUserAndType(tenantId, user.getId(), UserIdentityType.ACCOUNT);
        return support.saveUserIdentity(UserIdentity.create(
                accountIdentityId,
                user.getId(),
                UserIdentityType.ACCOUNT,
                requireIdentityValue(account, UserIdentityType.ACCOUNT),
                ACTIVE_IDENTITY_STATUS));
    }

    private void replacePhoneIdentity(TenantId tenantId, User user, String phone, UserIdentityId phoneIdentityId) {
        support.deleteUserIdentitiesByUserAndType(tenantId, user.getId(), UserIdentityType.PHONE);
        if (phone != null && !phone.isBlank()) {
            support.saveUserIdentity(UserIdentity.create(
                    phoneIdentityId, user.getId(), UserIdentityType.PHONE, phone.trim(), ACTIVE_IDENTITY_STATUS));
        }
    }

    private UserIdentity requireUserIdentity(UserId userId, UserIdentityType identityType) {
        return support.findUserIdentityByUserId(userId, identityType)
                .orElseThrow(() -> new NotFoundException(
                        "User identity not found: " + userId + "/" + identityType.value()));
    }

    private String requireIdentityValue(String identityValue, UserIdentityType identityType) {
        if (identityValue == null || identityValue.isBlank()) {
            throw new BadRequestException(identityType.value() + " identity must not be blank");
        }
        return identityValue.trim();
    }

    private String resolvePasswordHash(TenantId tenantId, User user, boolean newUser) {
        if (newUser) {
            return passwordEncoder.encode(DEFAULT_PASSWORD);
        }
        return support.findUserCredential(user.getId(), PASSWORD_CREDENTIAL_TYPE)
                .map(UserCredential::getCredentialValue)
                .orElseGet(() -> passwordEncoder.encode(DEFAULT_PASSWORD));
    }

    private void upsertPasswordCredential(
            TenantId tenantId,
            User user,
            UserIdentity accountIdentity,
            String passwordHash,
            boolean newUser,
            boolean needChangePassword,
            UserCredentialId passwordCredentialIdIfAbsent) {
        UserCredential currentCredential = support.findUserCredential(user.getId(), PASSWORD_CREDENTIAL_TYPE)
                .orElse(null);
        support.saveUserCredential(UserCredential.create(
                currentCredential == null ? passwordCredentialIdIfAbsent : currentCredential.getId(),
                user.getId(),
                accountIdentity.getId(),
                PASSWORD_CREDENTIAL_TYPE,
                PRIMARY_FACTOR_LEVEL,
                passwordHash,
                UserCredentialStatus.ACTIVE,
                newUser || needChangePassword,
                0,
                PASSWORD_FAILED_LIMIT,
                null,
                null,
                Instant.now().plus(PASSWORD_EXPIRE_DAYS, ChronoUnit.DAYS),
                currentCredential == null ? null : currentCredential.getLastVerifiedAt()));
    }

    boolean hasActiveUserInDepartment(TenantId tenantId, DepartmentId departmentId) {
        return support.hasActiveUserInDepartment(departmentId);
    }

    boolean hasActiveUserInDepartment(DepartmentId departmentId) {
        return support.hasActiveUserInDepartment(departmentId);
    }

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
