package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean({UserPersistenceSupport.class, RolePersistenceSupport.class})
public class UserRepositoryImpl implements UserRepository {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final String PASSWORD_CREDENTIAL_TYPE = "PASSWORD";
    private static final String PRIMARY_FACTOR_LEVEL = "PRIMARY";
    private static final String ACTIVE_CREDENTIAL_STATUS = "ACTIVE";
    private static final int PASSWORD_FAILED_LIMIT = 5;
    private static final long PASSWORD_EXPIRE_DAYS = 90L;

    private final UserPersistenceSupport support;
    private final RoleRepositoryImpl roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UpmsPermissionCacheSupport cacheSupport;

    public UserRepositoryImpl(UserPersistenceSupport support,
                              RoleRepositoryImpl roleRepository,
                              PasswordEncoder passwordEncoder,
                              UpmsPermissionCacheSupport cacheSupport) {
        this.support = support;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Optional<User> findUserById(Long tenantId, Long userId) {
        return support.findUserById(tenantId, userId);
    }

    @Override
    public Optional<User> findUserByAccount(Long tenantId, String account) {
        return support.findUserByAccount(tenantId, account);
    }

    @Override
    public Optional<UserIdentity> findUserIdentity(Long tenantId, String identityType, String identityValue) {
        return support.findUserIdentity(tenantId, identityType, identityValue);
    }

    @Override
    public Optional<UserCredential> findUserCredential(Long tenantId, Long userId, String credentialType) {
        return support.findUserCredential(tenantId, userId, credentialType);
    }

    @Override
    public List<User> pageUsers(Long tenantId, String account, String name, String phone, String status, int pageNo, int pageSize) {
        return support.listUsers(tenantId, account, name, phone, status, pageNo, pageSize);
    }

    @Override
    public long countUsers(Long tenantId, String account, String name, String phone, String status) {
        return support.countUsers(tenantId, account, name, phone, status);
    }

    @Override
    public List<User> listUsers(Long tenantId, String account, String name, String phone, String status) {
        return support.listUsers(tenantId, account, name, phone, status, 1, Integer.MAX_VALUE);
    }

    @Override
    public User save(User user) {
        boolean newUser = user.getId() == null;
        User savedUser = user.getId() == null ? createUser(user) : updateUser(user);
        savedUser = support.saveUser(savedUser);
        UserIdentity accountIdentity = replaceAccountIdentity(savedUser);
        upsertPasswordCredential(savedUser, accountIdentity, newUser, false);
        replacePhoneIdentity(savedUser);
        return savedUser;
    }

    @Override
    public User updatePassword(Long tenantId, Long userId, String password, boolean needChangePassword) {
        User currentUser = findUserById(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        User updatedUser = new User(
                currentUser.getId(),
                currentUser.getTenantId(),
                currentUser.getAccount(),
                currentUser.getName(),
                currentUser.getAvatarObjectId(),
                currentUser.getPhone(),
                passwordEncoder.encode(password),
                currentUser.getDepartmentId(),
                currentUser.getStatus(),
                currentUser.getCreatedBy(),
                currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt());
        User savedUser = support.saveUser(updatedUser);
        UserIdentity accountIdentity = replaceAccountIdentity(savedUser);
        upsertPasswordCredential(savedUser, accountIdentity, false, needChangePassword);
        return savedUser;
    }

    @Override
    public List<Role> assignRoles(Long tenantId, Long userId, List<Long> roleIds) {
        List<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository.findRoleById(tenantId, roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId)))
                .toList();
        roleRepository.bindUserRoles(tenantId, userId, roles);
        cacheSupport.evictUserPermission(tenantId, userId);
        return roles;
    }

    @Override
    public void deleteUser(Long tenantId, Long userId) {
        support.deleteUser(tenantId, userId);
        roleRepository.clearUserRoles(tenantId, userId);
        support.deleteUserIdentitiesByUser(tenantId, userId);
        support.deleteUserCredentialsByUser(tenantId, userId);
        cacheSupport.evictUserPermission(tenantId, userId);
    }

    private User createUser(User user) {
        return new User(null, user.getTenantId(), user.getAccount(), user.getName(), user.getAvatarObjectId(),
                user.getPhone(), passwordEncoder.encode(DEFAULT_PASSWORD), user.getDepartmentId(),
                user.getStatus());
    }

    private User updateUser(User user) {
        User currentUser = findUserById(user.getTenantId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
        return new User(
                currentUser.getId(),
                user.getTenantId(),
                user.getAccount(),
                user.getName(),
                user.getAvatarObjectId(),
                user.getPhone(),
                user.getPasswordHash() == null ? currentUser.getPasswordHash() : user.getPasswordHash(),
                user.getDepartmentId(),
                user.getStatus(),
                currentUser.getCreatedBy(),
                currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt());
    }

    private UserIdentity replaceAccountIdentity(User user) {
        support.deleteUserIdentitiesByUserAndType(user.getTenantId(), user.getId(), "ACCOUNT");
        return support.saveUserIdentity(new UserIdentity(null, user.getTenantId(), user.getId(), "ACCOUNT",
                user.getAccount(), true));
    }

    private void replacePhoneIdentity(User user) {
        support.deleteUserIdentitiesByUserAndType(user.getTenantId(), user.getId(), "PHONE");
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            support.saveUserIdentity(new UserIdentity(null, user.getTenantId(), user.getId(), "PHONE", user.getPhone(), true));
        }
    }

    private void upsertPasswordCredential(User user, UserIdentity accountIdentity, boolean newUser,
                                          boolean needChangePassword) {
        UserCredential currentCredential = support.findUserCredential(user.getTenantId(), user.getId(), PASSWORD_CREDENTIAL_TYPE)
                .orElse(null);
        support.saveUserCredential(new UserCredential(
                currentCredential == null ? null : currentCredential.getId(),
                user.getTenantId(),
                user.getId(),
                accountIdentity.getId(),
                PASSWORD_CREDENTIAL_TYPE,
                PRIMARY_FACTOR_LEVEL,
                user.getPasswordHash(),
                ACTIVE_CREDENTIAL_STATUS,
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

    boolean hasActiveUserInDepartment(Long tenantId, Long departmentId) {
        return support.hasActiveUserInDepartment(tenantId, departmentId);
    }
}
