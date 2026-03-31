package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(UpmsRepositorySupport.class)
public class UserRepositoryImpl implements UserRepository {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UpmsRepositorySupport support;
    private final RoleRepositoryImpl roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRepositoryImpl(UpmsRepositorySupport support, RoleRepositoryImpl roleRepository, PasswordEncoder passwordEncoder) {
        this.support = support;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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
        User savedUser = user.getId() == null ? createUser(user) : updateUser(user);
        savedUser = support.saveUser(savedUser);
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
        return support.saveUser(updatedUser);
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
        support.deleteUser(tenantId, userId);
        roleRepository.clearUserRoles(tenantId, userId);
        support.deleteUserIdentitiesByUser(tenantId, userId);
    }

    private User createUser(User user) {
        return new User(null, user.getTenantId(), user.getAccount(), user.getName(), user.getPhone(),
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
        support.deleteUserIdentitiesByUserAndType(tenantId, userId, identityType);
        support.saveUserIdentity(new UserIdentity(null, tenantId, userId, identityType, identityValue, true));
    }

    private void replacePhoneIdentity(User user) {
        support.deleteUserIdentitiesByUserAndType(user.getTenantId(), user.getId(), "PHONE");
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            support.saveUserIdentity(new UserIdentity(null, user.getTenantId(), user.getId(), "PHONE", user.getPhone(), true));
        }
    }

    boolean hasActiveUserInDepartment(Long tenantId, Long departmentId) {
        return support.hasActiveUserInDepartment(tenantId, departmentId);
    }
}
