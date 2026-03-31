package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final String DEFAULT_PASSWORD = "123456";

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, UserIdentity> userIdentities = new ConcurrentHashMap<>();
    private final AtomicLong userIdSequence = new AtomicLong(2002L);
    private final AtomicLong userIdentityIdSequence = new AtomicLong(3002L);
    private final RoleRepositoryImpl roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRepositoryImpl(RoleRepositoryImpl roleRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findUserById(Long tenantId, Long userId) {
        return Optional.ofNullable(users.get(UpmsRepositoryHelper.userKey(tenantId, userId)));
    }

    @Override
    public Optional<User> findUserByAccount(Long tenantId, String account) {
        return users.values().stream()
                .filter(user -> user.getTenantId().equals(tenantId))
                .filter(user -> !user.isDeleted())
                .filter(user -> user.getAccount().equals(account))
                .findFirst();
    }

    @Override
    public Optional<UserIdentity> findUserIdentity(Long tenantId, String identityType, String identityValue) {
        return Optional.ofNullable(userIdentities.get(UpmsRepositoryHelper.identityKey(tenantId, identityType, identityValue)));
    }

    @Override
    public List<User> pageUsers(Long tenantId, String account, String name, String phone, String status, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return filteredUsers(tenantId, account, name, phone, status).stream()
                .skip(offset)
                .limit(pageSize)
                .toList();
    }

    @Override
    public long countUsers(Long tenantId, String account, String name, String phone, String status) {
        return filteredUsers(tenantId, account, name, phone, status).size();
    }

    @Override
    public List<User> listUsers(Long tenantId, String account, String name, String phone, String status) {
        return filteredUsers(tenantId, account, name, phone, status);
    }

    @Override
    public User save(User user) {
        User savedUser = user.getId() == null ? createUser(user) : updateUser(user);
        users.put(UpmsRepositoryHelper.userKey(savedUser.getTenantId(), savedUser.getId()), savedUser);
        replaceIdentity(savedUser.getTenantId(), "ACCOUNT", savedUser.getAccount(), savedUser.getId());
        replacePhoneIdentity(savedUser);
        return savedUser;
    }

    @Override
    public User updatePassword(Long tenantId, Long userId, String password) {
        User currentUser = findUserById(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        User updatedUser = new User(
                currentUser.getId(),
                currentUser.getTenantId(),
                currentUser.getAccount(),
                currentUser.getName(),
                currentUser.getPhone(),
                passwordEncoder.encode(password),
                currentUser.getDepartmentId(),
                currentUser.getStatus(),
                currentUser.isDeleted(),
                currentUser.getCreatedBy(),
                currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt());
        users.put(UpmsRepositoryHelper.userKey(tenantId, userId), updatedUser);
        return updatedUser;
    }

    @Override
    public List<Role> assignRoles(Long tenantId, Long userId, List<Long> roleIds) {
        List<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository.findRoleById(tenantId, roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId)))
                .toList();
        roleRepository.bindUserRoles(tenantId, userId, roles);
        return roles;
    }

    @Override
    public void deleteUser(Long tenantId, Long userId) {
        users.remove(UpmsRepositoryHelper.userKey(tenantId, userId));
        roleRepository.clearUserRoles(tenantId, userId);
        userIdentities.entrySet().removeIf(entry ->
                entry.getValue().getTenantId().equals(tenantId) && entry.getValue().getUserId().equals(userId));
    }

    private List<User> filteredUsers(Long tenantId, String account, String name, String phone, String status) {
        return users.values().stream()
                .filter(user -> user.getTenantId().equals(tenantId))
                .filter(user -> !user.isDeleted())
                .filter(user -> matchContains(user.getAccount(), account))
                .filter(user -> matchContains(user.getName(), name))
                .filter(user -> matchContains(user.getPhone(), phone))
                .filter(user -> matchEquals(user.getStatus(), status))
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    private boolean matchContains(String actual, String expected) {
        return expected == null || expected.isBlank() || (actual != null && actual.contains(expected.trim()));
    }

    private boolean matchEquals(String actual, String expected) {
        return expected == null || expected.isBlank() || (actual != null && actual.equalsIgnoreCase(expected.trim()));
    }

    private User createUser(User user) {
        Long userId = userIdSequence.getAndIncrement();
        return new User(userId, user.getTenantId(), user.getAccount(), user.getName(), user.getPhone(),
                passwordEncoder.encode(DEFAULT_PASSWORD), user.getDepartmentId(), user.getStatus(), false);
    }

    private User updateUser(User user) {
        User currentUser = findUserById(user.getTenantId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
        return new User(
                currentUser.getId(),
                user.getTenantId(),
                user.getAccount(),
                user.getName(),
                user.getPhone(),
                user.getPasswordHash() == null ? currentUser.getPasswordHash() : user.getPasswordHash(),
                user.getDepartmentId(),
                user.getStatus(),
                user.isDeleted(),
                currentUser.getCreatedBy(),
                currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt());
    }

    private void replaceIdentity(Long tenantId, String identityType, String identityValue, Long userId) {
        userIdentities.entrySet().removeIf(entry ->
                entry.getValue().getTenantId().equals(tenantId)
                        && entry.getValue().getUserId().equals(userId)
                        && entry.getValue().getIdentityType().equals(identityType));
        userIdentities.put(UpmsRepositoryHelper.identityKey(tenantId, identityType, identityValue),
                new UserIdentity(userIdentityIdSequence.getAndIncrement(), tenantId, userId, identityType, identityValue, true));
    }

    private void replacePhoneIdentity(User user) {
        userIdentities.entrySet().removeIf(entry ->
                entry.getValue().getTenantId().equals(user.getTenantId())
                        && entry.getValue().getUserId().equals(user.getId())
                        && entry.getValue().getIdentityType().equals("PHONE"));
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            userIdentities.put(UpmsRepositoryHelper.identityKey(user.getTenantId(), "PHONE", user.getPhone()),
                    new UserIdentity(userIdentityIdSequence.getAndIncrement(), user.getTenantId(), user.getId(), "PHONE",
                            user.getPhone(), true));
        }
    }

    boolean hasActiveUserInDepartment(Long tenantId, Long departmentId) {
        return users.values().stream()
                .filter(user -> user.getTenantId().equals(tenantId))
                .filter(user -> !user.isDeleted())
                .anyMatch(user -> departmentId.equals(user.getDepartmentId()));
    }
}
