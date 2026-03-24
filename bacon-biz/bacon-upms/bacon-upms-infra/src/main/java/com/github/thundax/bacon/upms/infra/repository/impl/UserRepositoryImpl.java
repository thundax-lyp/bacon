package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.Role;
import com.github.thundax.bacon.upms.domain.entity.Tenant;
import com.github.thundax.bacon.upms.domain.entity.User;
import com.github.thundax.bacon.upms.domain.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final String DEFAULT_PASSWORD = "123456";

    private final InMemoryUpmsStore upmsStore;
    private final PasswordEncoder passwordEncoder;

    public UserRepositoryImpl(InMemoryUpmsStore upmsStore, PasswordEncoder passwordEncoder) {
        this.upmsStore = upmsStore;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findUserById(Long tenantId, Long userId) {
        return Optional.ofNullable(upmsStore.getUsers().get(InMemoryUpmsStore.userKey(tenantId, userId)));
    }

    @Override
    public Optional<User> findUserByAccount(Long tenantId, String account) {
        return upmsStore.getUsers().values().stream()
                .filter(user -> user.getTenantId().equals(tenantId))
                .filter(user -> !user.isDeleted())
                .filter(user -> user.getAccount().equals(account))
                .findFirst();
    }

    @Override
    public Optional<UserIdentity> findUserIdentity(Long tenantId, String identityType, String identityValue) {
        return Optional.ofNullable(upmsStore.getUserIdentities()
                .get(InMemoryUpmsStore.identityKey(tenantId, identityType, identityValue)));
    }

    @Override
    public Optional<Tenant> findTenantByTenantId(Long tenantId) {
        return Optional.ofNullable(upmsStore.getTenants().get(tenantId));
    }

    @Override
    public Optional<Tenant> findTenantByCode(String code) {
        return upmsStore.getTenants().values().stream()
                .filter(tenant -> tenant.getCode().equals(code))
                .findFirst();
    }

    @Override
    public List<Tenant> pageTenants(Long tenantId, String code, String name, String status, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return filteredTenants(tenantId, code, name, status).stream()
                .skip(offset)
                .limit(pageSize)
                .toList();
    }

    @Override
    public long countTenants(Long tenantId, String code, String name, String status) {
        return filteredTenants(tenantId, code, name, status).size();
    }

    @Override
    public Tenant saveTenant(Tenant tenant) {
        Tenant savedTenant;
        if (tenant.getId() == null) {
            Long generatedTenantId = upmsStore.nextTenantId();
            savedTenant = new Tenant(generatedTenantId, generatedTenantId, tenant.getCode(), tenant.getName(), tenant.getStatus());
        } else {
            savedTenant = tenant;
        }
        upmsStore.getTenants().put(savedTenant.getTenantId(), savedTenant);
        return savedTenant;
    }

    @Override
    public Tenant updateTenantStatus(Long tenantId, String status) {
        Tenant currentTenant = findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        Tenant updatedTenant = new Tenant(currentTenant.getId(), currentTenant.getCreatedBy(), currentTenant.getCreatedAt(),
                currentTenant.getUpdatedBy(), currentTenant.getUpdatedAt(), currentTenant.getTenantId(), currentTenant.getCode(),
                currentTenant.getName(), status);
        upmsStore.getTenants().put(tenantId, updatedTenant);
        return updatedTenant;
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
        upmsStore.getUsers().put(InMemoryUpmsStore.userKey(savedUser.getTenantId(), savedUser.getId()), savedUser);
        replaceIdentity(savedUser.getTenantId(), "ACCOUNT", savedUser.getAccount(), savedUser.getId());
        replacePhoneIdentity(savedUser);
        return savedUser;
    }

    @Override
    public User updatePassword(Long tenantId, Long userId, String password) {
        User currentUser = findUserById(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        User updatedUser = new User(currentUser.getId(), currentUser.getCreatedBy(), currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(), currentUser.getUpdatedAt(), currentUser.getTenantId(), currentUser.getAccount(),
                currentUser.getName(), currentUser.getPhone(), passwordEncoder.encode(password), currentUser.getDepartmentId(),
                currentUser.getStatus(),
                currentUser.isDeleted());
        upmsStore.getUsers().put(InMemoryUpmsStore.userKey(tenantId, userId), updatedUser);
        return updatedUser;
    }

    @Override
    public List<Role> assignRoles(Long tenantId, Long userId, List<Long> roleIds) {
        List<Role> roles = roleIds.stream()
                .map(roleId -> Optional.ofNullable(upmsStore.getRoles().get(InMemoryUpmsStore.roleKey(tenantId, roleId)))
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId)))
                .toList();
        upmsStore.getUserRoles().put(InMemoryUpmsStore.userKey(tenantId, userId), roles);
        return roles;
    }

    @Override
    public void deleteUser(Long tenantId, Long userId) {
        upmsStore.getUsers().remove(InMemoryUpmsStore.userKey(tenantId, userId));
        upmsStore.getUserRoles().remove(InMemoryUpmsStore.userKey(tenantId, userId));
        upmsStore.getUserMenus().remove(InMemoryUpmsStore.userKey(tenantId, userId));
        upmsStore.getUserPermissions().remove(InMemoryUpmsStore.userKey(tenantId, userId));
        upmsStore.getUserDepartmentScopes().remove(InMemoryUpmsStore.userKey(tenantId, userId));
        upmsStore.getUserScopeTypes().remove(InMemoryUpmsStore.userKey(tenantId, userId));
        upmsStore.getUserAllAccess().remove(InMemoryUpmsStore.userKey(tenantId, userId));
        upmsStore.getUserIdentities().entrySet().removeIf(entry ->
                entry.getValue().getTenantId().equals(tenantId) && entry.getValue().getUserId().equals(userId));
    }

    private List<User> filteredUsers(Long tenantId, String account, String name, String phone, String status) {
        return upmsStore.getUsers().values().stream()
                .filter(user -> user.getTenantId().equals(tenantId))
                .filter(user -> !user.isDeleted())
                .filter(user -> matchContains(user.getAccount(), account))
                .filter(user -> matchContains(user.getName(), name))
                .filter(user -> matchContains(user.getPhone(), phone))
                .filter(user -> matchEquals(user.getStatus(), status))
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    private List<Tenant> filteredTenants(Long tenantId, String code, String name, String status) {
        return upmsStore.getTenants().values().stream()
                .filter(tenant -> tenantId == null || tenantId.equals(tenant.getTenantId()))
                .filter(tenant -> matchContains(tenant.getCode(), code))
                .filter(tenant -> matchContains(tenant.getName(), name))
                .filter(tenant -> matchEquals(tenant.getStatus(), status))
                .sorted(Comparator.comparing(Tenant::getTenantId))
                .toList();
    }

    private boolean matchContains(String actual, String expected) {
        return expected == null || expected.isBlank() || (actual != null && actual.contains(expected.trim()));
    }

    private boolean matchEquals(String actual, String expected) {
        return expected == null || expected.isBlank() || (actual != null && actual.equalsIgnoreCase(expected.trim()));
    }

    private User createUser(User user) {
        Long userId = upmsStore.nextUserId();
        return new User(userId, user.getTenantId(), user.getAccount(), user.getName(), user.getPhone(),
                passwordEncoder.encode(DEFAULT_PASSWORD), user.getDepartmentId(), user.getStatus(), false);
    }

    private User updateUser(User user) {
        User currentUser = findUserById(user.getTenantId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
        return new User(currentUser.getId(), currentUser.getCreatedBy(), currentUser.getCreatedAt(), currentUser.getUpdatedBy(),
                currentUser.getUpdatedAt(), user.getTenantId(), user.getAccount(), user.getName(), user.getPhone(),
                user.getPasswordHash() == null ? currentUser.getPasswordHash() : user.getPasswordHash(), user.getDepartmentId(),
                user.getStatus(), user.isDeleted());
    }

    private void replaceIdentity(Long tenantId, String identityType, String identityValue, Long userId) {
        upmsStore.getUserIdentities().entrySet().removeIf(entry ->
                entry.getValue().getTenantId().equals(tenantId)
                        && entry.getValue().getUserId().equals(userId)
                        && entry.getValue().getIdentityType().equals(identityType));
        upmsStore.getUserIdentities().put(InMemoryUpmsStore.identityKey(tenantId, identityType, identityValue),
                new UserIdentity(upmsStore.nextUserIdentityId(), tenantId, userId, identityType, identityValue, true));
    }

    private void replacePhoneIdentity(User user) {
        upmsStore.getUserIdentities().entrySet().removeIf(entry ->
                entry.getValue().getTenantId().equals(user.getTenantId())
                        && entry.getValue().getUserId().equals(user.getId())
                        && entry.getValue().getIdentityType().equals("PHONE"));
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            upmsStore.getUserIdentities().put(InMemoryUpmsStore.identityKey(user.getTenantId(), "PHONE", user.getPhone()),
                    new UserIdentity(upmsStore.nextUserIdentityId(), user.getTenantId(), user.getId(), "PHONE",
                            user.getPhone(), true));
        }
    }
}
