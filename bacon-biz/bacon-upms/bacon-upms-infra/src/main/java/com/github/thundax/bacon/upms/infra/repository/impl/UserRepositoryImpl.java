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
    public Optional<User> findById(UserId userId) {
        return support.findById(userId);
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return support.findByAccount(account);
    }

    @Override
    public Optional<UserIdentity> findIdentity(UserIdentityType identityType, String identityValue) {
        return support.findIdentity(identityType, identityValue);
    }

    @Override
    public Optional<UserIdentity> findIdentityByUserId(UserId userId, UserIdentityType identityType) {
        return support.findIdentityByUserId(userId, identityType);
    }

    @Override
    public Optional<UserCredential> findCredentialByUserId(UserId userId, UserCredentialType credentialType) {
        return support.findCredentialByUserId(userId, credentialType);
    }

    @Override
    public List<User> page(
            String account, String name, String phone, UserStatus status, int pageNo, int pageSize) {
        return support.listUsers(account, name, phone, status, pageNo, pageSize);
    }

    @Override
    public long count(String account, String name, String phone, UserStatus status) {
        return support.count(account, name, phone, status);
    }

    @Override
    public List<User> list(String account, String name, String phone, UserStatus status) {
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
                findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        User savedUser = support.updateUser(copyUser(currentUser));
        UserIdentity accountIdentity = requireUserIdentity(userId, UserIdentityType.ACCOUNT);
        upsertPasswordCredential(
                savedUser,
                accountIdentity,
                passwordEncoder.encode(password),
                false,
                needChangePassword,
                passwordCredentialIdIfAbsent);
        return savedUser;
    }

    @Override
    public List<Role> updateRoleIds(UserId userId, List<RoleId> roleIds) {
        TenantId tenantId = requireTenantId();
        List<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository
                        .findById(roleId)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value())))
                .toList();
        roleRepository.bindUserRoles(tenantId, userId, roles);
        cacheSupport.evictUserPermission(tenantId, userId);
        return roles;
    }

    @Override
    public void delete(UserId userId) {
        TenantId tenantId = requireTenantId();
        support.delete(userId);
        roleRepository.clearUserRoles(tenantId, userId);
        support.deleteUserIdentitiesByUser(tenantId, userId);
        support.deleteUserCredentialsByUser(tenantId, userId);
        cacheSupport.evictUserPermission(tenantId, userId);
    }

    private User copyUser(User user) {
        return User.reconstruct(
                user.getId(),
                user.getNickname(),
                user.getAvatarStoredObjectNo(),
                user.getDepartmentId(),
                user.getStatus());
    }

    private UserIdentity replaceAccountIdentity(
            TenantId tenantId, User user, String account, UserIdentityId accountIdentityId) {
        String normalizedAccount = requireIdentityValue(account, UserIdentityType.ACCOUNT);
        UserIdentity currentIdentity = support.findIdentityByUserId(user.getId(), UserIdentityType.ACCOUNT)
                .orElse(null);
        if (currentIdentity == null) {
            return support.saveUserIdentity(UserIdentity.create(
                    accountIdentityId, user.getId(), UserIdentityType.ACCOUNT, normalizedAccount, ACTIVE_IDENTITY_STATUS));
        }
        currentIdentity.changeAccount(normalizedAccount);
        return support.saveUserIdentity(currentIdentity);
    }

    private void replacePhoneIdentity(TenantId tenantId, User user, String phone, UserIdentityId phoneIdentityId) {
        if (phone == null || phone.isBlank()) {
            support.deleteUserIdentitiesByUserAndType(tenantId, user.getId(), UserIdentityType.PHONE);
            return;
        }
        String normalizedPhone = phone.trim();
        UserIdentity currentIdentity = support.findIdentityByUserId(user.getId(), UserIdentityType.PHONE)
                .orElse(null);
        if (currentIdentity == null) {
            support.saveUserIdentity(UserIdentity.create(
                    phoneIdentityId, user.getId(), UserIdentityType.PHONE, normalizedPhone, ACTIVE_IDENTITY_STATUS));
            return;
        }
        currentIdentity.changePhone(normalizedPhone);
        support.saveUserIdentity(currentIdentity);
    }

    private UserIdentity requireUserIdentity(UserId userId, UserIdentityType identityType) {
        return support.findIdentityByUserId(userId, identityType)
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
        return support.findCredentialByUserId(user.getId(), PASSWORD_CREDENTIAL_TYPE)
                .map(UserCredential::getCredentialValue)
                .orElseGet(() -> passwordEncoder.encode(DEFAULT_PASSWORD));
    }

    private void upsertPasswordCredential(
            User user,
            UserIdentity accountIdentity,
            String passwordHash,
            boolean newUser,
            boolean needChangePassword,
            UserCredentialId passwordCredentialIdIfAbsent) {
        UserCredential currentCredential = support.findCredentialByUserId(user.getId(), PASSWORD_CREDENTIAL_TYPE)
                .orElse(null);
        Instant passwordExpiresAt = Instant.now().plus(PASSWORD_EXPIRE_DAYS, ChronoUnit.DAYS);
        if (currentCredential == null) {
            support.saveUserCredential(UserCredential.create(
                    passwordCredentialIdIfAbsent,
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
                    passwordExpiresAt,
                    null));
            return;
        }
        currentCredential.bindIdentity(accountIdentity.getId());
        currentCredential.replacePassword(passwordHash, newUser || needChangePassword, passwordExpiresAt);
        support.saveUserCredential(currentCredential);
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
