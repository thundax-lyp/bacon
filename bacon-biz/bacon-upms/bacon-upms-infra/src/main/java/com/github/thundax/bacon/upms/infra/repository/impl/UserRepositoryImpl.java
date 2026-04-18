package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class UserRepositoryImpl implements UserRepository {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final UserCredentialType PASSWORD_CREDENTIAL_TYPE = UserCredentialType.PASSWORD;
    private static final int PASSWORD_FAILED_LIMIT = 5;
    private static final long PASSWORD_EXPIRE_DAYS = 90L;
    private final UserPersistenceSupport userSupport;
    private final UserIdentityPersistenceSupport userIdentitySupport;
    private final UserCredentialPersistenceSupport userCredentialSupport;
    private final UserRolePersistenceSupport userRoleSupport;
    private final PasswordEncoder passwordEncoder;
    private final UpmsPermissionCacheSupport cacheSupport;

    public UserRepositoryImpl(
            UserPersistenceSupport userSupport,
            UserIdentityPersistenceSupport userIdentitySupport,
            UserCredentialPersistenceSupport userCredentialSupport,
            UserRolePersistenceSupport userRoleSupport,
            PasswordEncoder passwordEncoder,
            UpmsPermissionCacheSupport cacheSupport) {
        this.userSupport = userSupport;
        this.userIdentitySupport = userIdentitySupport;
        this.userCredentialSupport = userCredentialSupport;
        this.userRoleSupport = userRoleSupport;
        this.passwordEncoder = passwordEncoder;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return userSupport.findById(userId);
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return userIdentitySupport
                .findIdentity(UserIdentityType.ACCOUNT, account)
                .flatMap(identity -> userSupport.findById(identity.getUserId()));
    }

    @Override
    public List<User> list(String account, String name, String phone, UserStatus status) {
        return queryUsers(account, name, phone, status, 1, Integer.MAX_VALUE);
    }

    @Override
    public List<User> page(
            String account, String name, String phone, UserStatus status, int pageNo, int pageSize) {
        return queryUsers(account, name, phone, status, pageNo, pageSize);
    }

    @Override
    public long count(String account, String name, String phone, UserStatus status) {
        Set<Long> userIds = userIdentitySupport.resolveUserIdsByIdentityFilters(account, phone);
        if (userIds != null && userIds.isEmpty()) {
            return 0L;
        }
        return userSupport.count(userIds, name, status);
    }

    @Override
    public boolean existsActiveByDepartmentId(DepartmentId departmentId) {
        return userSupport.existsActiveByDepartmentId(departmentId);
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
        User savedUser = copyUser(user);
        savedUser = newUser ? userSupport.insert(savedUser) : userSupport.update(savedUser);
        UserIdentity accountIdentity = replaceAccountIdentity(savedUser, account, accountIdentityId);
        upsertPasswordCredential(
                savedUser,
                accountIdentity,
                resolvePasswordHash(savedUser, newUser),
                newUser,
                false,
                passwordCredentialIdIfAbsent);
        replacePhoneIdentity(savedUser, phone, phoneIdentityId);
        return savedUser;
    }

    @Override
    public User updatePassword(
            UserId userId, String password, boolean needChangePassword, UserCredentialId passwordCredentialIdIfAbsent) {
        User currentUser =
                findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        User savedUser = userSupport.update(copyUser(currentUser));
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
    public void delete(UserId userId) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        userSupport.delete(userId);
        userRoleSupport.deleteRoleIdsByUserId(userId);
        userIdentitySupport.deleteIdentitiesByUserId(userId);
        userCredentialSupport.deleteCredentialsByUserId(userId);
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

    private UserIdentity replaceAccountIdentity(User user, String account, UserIdentityId accountIdentityId) {
        String normalizedAccount = requireIdentityValue(account, UserIdentityType.ACCOUNT);
        UserIdentity currentIdentity = userIdentitySupport.findIdentityByUserId(user.getId(), UserIdentityType.ACCOUNT)
                .orElse(null);
        if (currentIdentity == null) {
            return userIdentitySupport.insert(UserIdentity.create(
                    accountIdentityId, user.getId(), UserIdentityType.ACCOUNT, normalizedAccount));
        }
        currentIdentity.changeAccount(normalizedAccount);
        return userIdentitySupport.update(currentIdentity);
    }

    private void replacePhoneIdentity(User user, String phone, UserIdentityId phoneIdentityId) {
        if (phone == null || phone.isBlank()) {
            userIdentitySupport.deleteIdentityByUserIdAndType(user.getId(), UserIdentityType.PHONE);
            return;
        }
        String normalizedPhone = phone.trim();
        UserIdentity currentIdentity = userIdentitySupport.findIdentityByUserId(user.getId(), UserIdentityType.PHONE)
                .orElse(null);
        if (currentIdentity == null) {
            userIdentitySupport.insert(
                    UserIdentity.create(phoneIdentityId, user.getId(), UserIdentityType.PHONE, normalizedPhone));
            return;
        }
        currentIdentity.changePhone(normalizedPhone);
        userIdentitySupport.update(currentIdentity);
    }

    private UserIdentity requireUserIdentity(UserId userId, UserIdentityType identityType) {
        return userIdentitySupport.findIdentityByUserId(userId, identityType)
                .orElseThrow(() -> new NotFoundException(
                        "User identity not found: " + userId + "/" + identityType.value()));
    }

    private String requireIdentityValue(String identityValue, UserIdentityType identityType) {
        if (identityValue == null || identityValue.isBlank()) {
            throw new BadRequestException(identityType.value() + " identity must not be blank");
        }
        return identityValue.trim();
    }

    private String resolvePasswordHash(User user, boolean newUser) {
        if (newUser) {
            return passwordEncoder.encode(DEFAULT_PASSWORD);
        }
        return userCredentialSupport.findCredentialByUserId(user.getId(), PASSWORD_CREDENTIAL_TYPE)
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
        UserCredential currentCredential =
                userCredentialSupport.findCredentialByUserId(user.getId(), PASSWORD_CREDENTIAL_TYPE).orElse(null);
        Instant passwordExpiresAt = Instant.now().plus(PASSWORD_EXPIRE_DAYS, ChronoUnit.DAYS);
        if (currentCredential == null) {
            userCredentialSupport.insert(UserCredential.createPassword(
                    passwordCredentialIdIfAbsent,
                    user.getId(),
                    accountIdentity.getId(),
                    passwordHash,
                    newUser || needChangePassword,
                    PASSWORD_FAILED_LIMIT,
                    passwordExpiresAt));
            return;
        }
        currentCredential.bindIdentity(accountIdentity.getId());
        currentCredential.replacePassword(passwordHash, newUser || needChangePassword, passwordExpiresAt);
        userCredentialSupport.update(currentCredential);
    }

    private List<User> queryUsers(
            String account, String name, String phone, UserStatus status, int pageNo, int pageSize) {
        Set<Long> userIds = userIdentitySupport.resolveUserIdsByIdentityFilters(account, phone);
        if (userIds != null && userIds.isEmpty()) {
            return List.of();
        }
        return userSupport.page(userIds, name, status, pageNo, pageSize);
    }
}
