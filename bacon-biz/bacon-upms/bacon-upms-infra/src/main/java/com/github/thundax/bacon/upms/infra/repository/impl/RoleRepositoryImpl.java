package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> userRoles = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> roleMenus = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roleResources = new ConcurrentHashMap<>();
    private final Map<String, String> roleDataScopeTypes = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> roleDataScopeDepartments = new ConcurrentHashMap<>();
    private final AtomicLong roleIdSequence = new AtomicLong(4002L);

    @Override
    public Optional<Role> findRoleById(Long tenantId, Long roleId) {
        return Optional.ofNullable(roles.get(UpmsRepositoryHelper.roleKey(tenantId, roleId)));
    }

    @Override
    public List<Role> findRolesByUserId(Long tenantId, Long userId) {
        return userRoles.getOrDefault(UpmsRepositoryHelper.userKey(tenantId, userId), List.of()).stream()
                .map(roleId -> roles.get(UpmsRepositoryHelper.roleKey(tenantId, roleId)))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public List<Role> pageRoles(Long tenantId, String code, String name, String roleType, String status, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return filteredRoles(tenantId, code, name, roleType, status).stream()
                .skip(offset)
                .limit(pageSize)
                .toList();
    }

    @Override
    public long countRoles(Long tenantId, String code, String name, String roleType, String status) {
        return filteredRoles(tenantId, code, name, roleType, status).size();
    }

    @Override
    public Role save(Role role) {
        Role savedRole = role.getId() == null
                ? new Role(roleIdSequence.getAndIncrement(), role.getTenantId(), role.getCode(), role.getName(), role.getRoleType(),
                role.getDataScopeType(), role.getStatus())
                : role;
        roles.put(UpmsRepositoryHelper.roleKey(savedRole.getTenantId(), savedRole.getId()), savedRole);
        return savedRole;
    }

    @Override
    public Role updateStatus(Long tenantId, Long roleId, String status) {
        Role currentRole = findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Role updatedRole = new Role(
                currentRole.getId(),
                currentRole.getTenantId(),
                currentRole.getCode(),
                currentRole.getName(),
                currentRole.getRoleType(),
                currentRole.getDataScopeType(),
                status,
                currentRole.getCreatedBy(),
                currentRole.getCreatedAt(),
                currentRole.getUpdatedBy(),
                currentRole.getUpdatedAt());
        roles.put(UpmsRepositoryHelper.roleKey(tenantId, roleId), updatedRole);
        return updatedRole;
    }

    @Override
    public void deleteRole(Long tenantId, Long roleId) {
        String roleKey = UpmsRepositoryHelper.roleKey(tenantId, roleId);
        roles.remove(roleKey);
        roleMenus.remove(roleKey);
        roleResources.remove(roleKey);
        roleDataScopeTypes.remove(roleKey);
        roleDataScopeDepartments.remove(roleKey);
        userRoles.replaceAll((key, assignedRoleIds) -> key.startsWith(tenantId + ":")
                ? assignedRoleIds.stream().filter(assignedRoleId -> !roleId.equals(assignedRoleId)).toList()
                : assignedRoleIds);
    }

    @Override
    public Set<Long> getAssignedMenus(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return roleMenus.getOrDefault(UpmsRepositoryHelper.roleKey(tenantId, roleId), Set.of());
    }

    @Override
    public Set<Long> assignMenus(Long tenantId, Long roleId, Set<Long> menuIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<Long> safeMenuIds = menuIds == null ? Set.of() : Set.copyOf(menuIds);
        roleMenus.put(UpmsRepositoryHelper.roleKey(tenantId, roleId), safeMenuIds);
        return safeMenuIds;
    }

    @Override
    public Set<String> getAssignedResources(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return roleResources.getOrDefault(UpmsRepositoryHelper.roleKey(tenantId, roleId), Set.of());
    }

    @Override
    public Set<String> assignResources(Long tenantId, Long roleId, Set<String> resourceCodes) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<String> safeResourceCodes = resourceCodes == null ? Set.of() : Set.copyOf(resourceCodes);
        roleResources.put(UpmsRepositoryHelper.roleKey(tenantId, roleId), safeResourceCodes);
        return safeResourceCodes;
    }

    @Override
    public String getAssignedDataScopeType(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return roleDataScopeTypes.getOrDefault(UpmsRepositoryHelper.roleKey(tenantId, roleId), "SELF");
    }

    @Override
    public Set<Long> getAssignedDataScopeDepartments(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return roleDataScopeDepartments.getOrDefault(UpmsRepositoryHelper.roleKey(tenantId, roleId), Set.of());
    }

    @Override
    public Set<Long> assignDataScope(Long tenantId, Long roleId, String dataScopeType, Set<Long> departmentIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<Long> safeDepartmentIds = departmentIds == null ? Set.of() : Set.copyOf(departmentIds);
        String roleKey = UpmsRepositoryHelper.roleKey(tenantId, roleId);
        roleDataScopeTypes.put(roleKey, dataScopeType);
        roleDataScopeDepartments.put(roleKey, safeDepartmentIds);
        Role currentRole = findRoleById(tenantId, roleId).orElseThrow();
        Role updatedRole = new Role(
                currentRole.getId(),
                currentRole.getTenantId(),
                currentRole.getCode(),
                currentRole.getName(),
                currentRole.getRoleType(),
                dataScopeType,
                currentRole.getStatus(),
                currentRole.getCreatedBy(),
                currentRole.getCreatedAt(),
                currentRole.getUpdatedBy(),
                currentRole.getUpdatedAt());
        roles.put(roleKey, updatedRole);
        return safeDepartmentIds;
    }

    private List<Role> filteredRoles(Long tenantId, String code, String name, String roleType, String status) {
        return roles.values().stream()
                .filter(role -> role.getTenantId().equals(tenantId))
                .filter(role -> matchContains(role.getCode(), code))
                .filter(role -> matchContains(role.getName(), name))
                .filter(role -> matchEquals(role.getRoleType(), roleType))
                .filter(role -> matchEquals(role.getStatus(), status))
                .sorted(Comparator.comparing(Role::getId))
                .toList();
    }

    private boolean matchContains(String actual, String expected) {
        return expected == null || expected.isBlank() || (actual != null && actual.contains(expected.trim()));
    }

    private boolean matchEquals(String actual, String expected) {
        return expected == null || expected.isBlank() || (actual != null && actual.equalsIgnoreCase(expected.trim()));
    }

    void bindUserRoles(Long tenantId, Long userId, List<Role> assignedRoles) {
        userRoles.put(UpmsRepositoryHelper.userKey(tenantId, userId), assignedRoles.stream()
                .map(Role::getId)
                .toList());
    }

    void clearUserRoles(Long tenantId, Long userId) {
        userRoles.remove(UpmsRepositoryHelper.userKey(tenantId, userId));
    }

    void removeMenuFromAssignments(Long tenantId, Long menuId) {
        roleMenus.replaceAll((key, menuIds) -> key.startsWith(tenantId + ":")
                ? menuIds.stream().filter(id -> !id.equals(menuId)).collect(java.util.stream.Collectors.toSet())
                : menuIds);
    }
}
