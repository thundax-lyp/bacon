package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.Department;
import com.github.thundax.bacon.upms.domain.entity.Menu;
import com.github.thundax.bacon.upms.domain.entity.Role;
import com.github.thundax.bacon.upms.domain.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.entity.Tenant;
import com.github.thundax.bacon.upms.domain.entity.User;
import com.github.thundax.bacon.upms.domain.entity.UserIdentity;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InMemoryUpmsStore {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, UserIdentity> userIdentities = new ConcurrentHashMap<>();
    private final Map<Long, Tenant> tenants = new ConcurrentHashMap<>();
    private final Map<String, Department> departments = new ConcurrentHashMap<>();
    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, List<Role>> userRoles = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> roleMenus = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roleResources = new ConcurrentHashMap<>();
    private final Map<String, String> roleDataScopeTypes = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> roleDataScopeDepartments = new ConcurrentHashMap<>();
    private final Map<String, List<Menu>> userMenus = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> userDepartmentScopes = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userScopeTypes = new ConcurrentHashMap<>();
    private final Map<String, Boolean> userAllAccess = new ConcurrentHashMap<>();
    private final Map<Long, SysLogRecord> sysLogs = new ConcurrentHashMap<>();
    private final AtomicLong userIdSequence = new AtomicLong(2002L);
    private final AtomicLong userIdentityIdSequence = new AtomicLong(3002L);
    private final AtomicLong roleIdSequence = new AtomicLong(4002L);
    private final AtomicLong departmentIdSequence = new AtomicLong(11L);
    private final AtomicLong tenantIdSequence = new AtomicLong(1002L);

    public InMemoryUpmsStore(PasswordEncoder passwordEncoder) {
        Tenant tenant = new Tenant(1L, 1001L, "tenant-demo", "Demo Tenant", "ENABLED");
        tenants.put(tenant.getTenantId(), tenant);

        Department rootDepartment = new Department(10L, 1001L, "ROOT", "Headquarters", 0L, 2001L, "ENABLED");
        departments.put(departmentKey(rootDepartment.getTenantId(), rootDepartment.getId()), rootDepartment);
        departments.put(departmentCodeKey(rootDepartment.getTenantId(), rootDepartment.getCode()), rootDepartment);

        User admin = new User(2001L, 1001L, "admin", "System Admin", "13800000000",
                passwordEncoder.encode("123456"), 10L, "ENABLED", false);
        users.put(userKey(admin.getTenantId(), admin.getId()), admin);

        UserIdentity accountIdentity = new UserIdentity(3001L, 1001L, 2001L, "ACCOUNT", "admin", true);
        userIdentities.put(identityKey(1001L, "ACCOUNT", "admin"), accountIdentity);

        Role adminRole = new Role(4001L, 1001L, "ADMIN", "Administrator", "SYSTEM_ROLE", "ALL", "ENABLED");
        roles.put(roleKey(adminRole.getTenantId(), adminRole.getId()), adminRole);
        userRoles.put(userKey(1001L, 2001L), List.of(adminRole));
        roleMenus.put(roleKey(1001L, 4001L), Set.of(5001L, 5002L));
        roleResources.put(roleKey(1001L, 4001L), Set.of("upms:user:view", "upms:user:save"));
        roleDataScopeTypes.put(roleKey(1001L, 4001L), "ALL");
        roleDataScopeDepartments.put(roleKey(1001L, 4001L), Set.of(10L));

        Menu button = new Menu(5002L, 1001L, "BUTTON", "Save Button", 5001L, "", "", "", 2, "upms:user:save", List.of());
        Menu menu = new Menu(5001L, 1001L, "MENU", "User Management", 0L, "/users", "UserPage", "users", 1,
                "upms:user:view", List.of(button));
        userMenus.put(userKey(1001L, 2001L), List.of(menu));
        userPermissions.put(userKey(1001L, 2001L), Set.of("upms:user:view", "upms:user:save"));
        userDepartmentScopes.put(userKey(1001L, 2001L), Set.of(10L));
        userScopeTypes.put(userKey(1001L, 2001L), Set.of("ALL"));
        userAllAccess.put(userKey(1001L, 2001L), true);
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, UserIdentity> getUserIdentities() {
        return userIdentities;
    }

    public Map<Long, Tenant> getTenants() {
        return tenants;
    }

    public Map<String, Department> getDepartments() {
        return departments;
    }

    public Map<String, Role> getRoles() {
        return roles;
    }

    public Map<String, List<Role>> getUserRoles() {
        return userRoles;
    }

    public Map<String, Set<Long>> getRoleMenus() {
        return roleMenus;
    }

    public Map<String, Set<String>> getRoleResources() {
        return roleResources;
    }

    public Map<String, String> getRoleDataScopeTypes() {
        return roleDataScopeTypes;
    }

    public Map<String, Set<Long>> getRoleDataScopeDepartments() {
        return roleDataScopeDepartments;
    }

    public Map<String, List<Menu>> getUserMenus() {
        return userMenus;
    }

    public Map<String, Set<String>> getUserPermissions() {
        return userPermissions;
    }

    public Map<String, Set<Long>> getUserDepartmentScopes() {
        return userDepartmentScopes;
    }

    public Map<String, Set<String>> getUserScopeTypes() {
        return userScopeTypes;
    }

    public Map<String, Boolean> getUserAllAccess() {
        return userAllAccess;
    }

    public Map<Long, SysLogRecord> getSysLogs() {
        return sysLogs;
    }

    public long nextUserId() {
        return userIdSequence.getAndIncrement();
    }

    public long nextUserIdentityId() {
        return userIdentityIdSequence.getAndIncrement();
    }

    public long nextRoleId() {
        return roleIdSequence.getAndIncrement();
    }

    public long nextDepartmentId() {
        return departmentIdSequence.getAndIncrement();
    }

    public long nextTenantId() {
        return tenantIdSequence.getAndIncrement();
    }

    public static String userKey(Long tenantId, Long userId) {
        return tenantId + ":" + userId;
    }

    public static String identityKey(Long tenantId, String identityType, String identityValue) {
        return tenantId + ":" + identityType + ":" + identityValue;
    }

    public static String departmentKey(Long tenantId, Long departmentId) {
        return tenantId + ":" + departmentId;
    }

    public static String departmentCodeKey(Long tenantId, String code) {
        return tenantId + ":" + code;
    }

    public static String roleKey(Long tenantId, Long roleId) {
        return tenantId + ":" + roleId;
    }
}
