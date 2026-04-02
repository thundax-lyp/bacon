package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
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

    private final UserPersistenceSupport support;
    private final RoleRepositoryImpl roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UpmsPermissionCacheSupport cacheSupport;
    private final Ids ids;

    public UserRepositoryImpl(UserPersistenceSupport support,
                              RoleRepositoryImpl roleRepository,
                              PasswordEncoder passwordEncoder,
                              UpmsPermissionCacheSupport cacheSupport,
                              Ids ids) {
        this.support = support;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheSupport = cacheSupport;
        this.ids = ids;
    }

    @Override
    public Optional<User> findUserById(TenantId tenantId, UserId userId) {
        return support.findUserById(tenantId, userId);
    }

    @Override
    public Optional<User> findUserByAccount(TenantId tenantId, String account) {
        return support.findUserByAccount(tenantId, account);
    }

    @Override
    public Optional<UserIdentity> findUserIdentity(TenantId tenantId, UserIdentityType identityType, String identityValue) {
        return support.findUserIdentity(tenantId, identityType, identityValue);
    }

    @Override
    public Optional<UserIdentity> findUserIdentityByUserId(TenantId tenantId, UserId userId, UserIdentityType identityType) {
        return support.findUserIdentityByUserId(tenantId, userId, identityType);
    }

    @Override
    public Optional<UserCredential> findUserCredential(TenantId tenantId, UserId userId, UserCredentialType credentialType) {
        return support.findUserCredential(tenantId, userId, credentialType);
    }

    @Override
    public List<User> pageUsers(TenantId tenantId, String account, String name, String phone, String status, int pageNo,
                                int pageSize) {
        return support.listUsers(tenantId, account, name, phone, status, pageNo, pageSize);
    }

    @Override
    public long countUsers(TenantId tenantId, String account, String name, String phone, String status) {
        return support.countUsers(tenantId, account, name, phone, status);
    }

    @Override
    public List<User> listUsers(TenantId tenantId, String account, String name, String phone, String status) {
        return support.listUsers(tenantId, account, name, phone, status, 1, Integer.MAX_VALUE);
    }

    @Override
    public User save(User user, String account, String phone) {
        boolean newUser = user.getId() == null;
        User savedUser = user.getId() == null ? createUser(user) : updateUser(user);
        savedUser = support.saveUser(savedUser);
        UserIdentity accountIdentity = replaceAccountIdentity(savedUser, account);
        upsertPasswordCredential(savedUser, accountIdentity, resolvePasswordHash(savedUser, newUser), newUser, false);
        replacePhoneIdentity(savedUser, phone);
        return savedUser;
    }

    @Override
    public User updatePassword(TenantId tenantId, UserId userId, String password, boolean needChangePassword) {
        User currentUser = findUserById(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        User updatedUser = new User(
                currentUser.getId(),
                currentUser.getTenantId(),
                currentUser.getName(),
                currentUser.getAvatarObjectId(),
                currentUser.getDepartmentId(),
                currentUser.getStatus(),
                currentUser.getCreatedBy(),
                currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt());
        User savedUser = support.saveUser(updatedUser);
        UserIdentity accountIdentity = requireUserIdentity(tenantId, userId, UserIdentityType.ACCOUNT);
        upsertPasswordCredential(savedUser, accountIdentity, passwordEncoder.encode(password), false, needChangePassword);
        return savedUser;
    }

    @Override
    public List<Role> assignRoles(TenantId tenantId, UserId userId, List<RoleId> roleIds) {
        List<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository.findRoleById(tenantId, roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value())))
                .toList();
        roleRepository.bindUserRoles(tenantId, userId, roles);
        cacheSupport.evictUserPermission(tenantId, userId);
        return roles;
    }

    @Override
    public void deleteUser(TenantId tenantId, UserId userId) {
        support.deleteUser(tenantId, userId);
        roleRepository.clearUserRoles(tenantId, userId);
        support.deleteUserIdentitiesByUser(tenantId, userId);
        support.deleteUserCredentialsByUser(tenantId, userId);
        cacheSupport.evictUserPermission(tenantId, userId);
    }

    private User createUser(User user) {
        return new User(ids.userId(), user.getTenantId(), user.getName(), user.getAvatarObjectId(),
                user.getDepartmentId(),
                user.getStatus());
    }

    private User updateUser(User user) {
        User currentUser = findUserById(user.getTenantId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
        return new User(
                currentUser.getId(),
                user.getTenantId(),
                user.getName(),
                user.getAvatarObjectId(),
                user.getDepartmentId(),
                user.getStatus(),
                currentUser.getCreatedBy(),
                currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt());
    }

    private UserIdentity replaceAccountIdentity(User user, String account) {
        support.deleteUserIdentitiesByUserAndType(user.getTenantId(), user.getId(), UserIdentityType.ACCOUNT);
        return support.saveUserIdentity(new UserIdentity(ids.userIdentityId(), user.getTenantId(), user.getId(),
                UserIdentityType.ACCOUNT, requireIdentityValue(account, UserIdentityType.ACCOUNT), true));
    }

    private void replacePhoneIdentity(User user, String phone) {
        support.deleteUserIdentitiesByUserAndType(user.getTenantId(), user.getId(), UserIdentityType.PHONE);
        if (phone != null && !phone.isBlank()) {
            support.saveUserIdentity(new UserIdentity(ids.userIdentityId(), user.getTenantId(), user.getId(),
                    UserIdentityType.PHONE, phone.trim(), true));
        }
    }

    private UserIdentity requireUserIdentity(TenantId tenantId, UserId userId, UserIdentityType identityType) {
        return support.findUserIdentityByUserId(tenantId, userId, identityType)
                .orElseThrow(() -> new IllegalArgumentException("User identity not found: " + userId + "/" + identityType.value()));
    }

    private String requireIdentityValue(String identityValue, UserIdentityType identityType) {
        if (identityValue == null || identityValue.isBlank()) {
            throw new IllegalArgumentException(identityType.value() + " identity must not be blank");
        }
        return identityValue.trim();
    }

    private String resolvePasswordHash(User user, boolean newUser) {
        if (newUser) {
            return passwordEncoder.encode(DEFAULT_PASSWORD);
        }
        return support.findUserCredential(user.getTenantId(), user.getId(), PASSWORD_CREDENTIAL_TYPE)
                .map(UserCredential::getCredentialValue)
                .orElseGet(() -> passwordEncoder.encode(DEFAULT_PASSWORD));
    }

    private void upsertPasswordCredential(User user, UserIdentity accountIdentity, String passwordHash, boolean newUser,
                                          boolean needChangePassword) {
        UserCredential currentCredential = support.findUserCredential(user.getTenantId(), user.getId(), PASSWORD_CREDENTIAL_TYPE)
                .orElse(null);
        support.saveUserCredential(new UserCredential(
                currentCredential == null ? ids.userCredentialId() : currentCredential.getId(),
                user.getTenantId(),
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
                currentCredential == null ? null : currentCredential.getLastVerifiedAt(),
                currentCredential == null ? null : currentCredential.getCreatedBy(),
                currentCredential == null ? null : currentCredential.getCreatedAt(),
                currentCredential == null ? null : currentCredential.getUpdatedBy(),
                currentCredential == null ? null : currentCredential.getUpdatedAt()));
    }

    boolean hasActiveUserInDepartment(TenantId tenantId, DepartmentId departmentId) {
        return support.hasActiveUserInDepartment(tenantId, departmentId);
    }
}
