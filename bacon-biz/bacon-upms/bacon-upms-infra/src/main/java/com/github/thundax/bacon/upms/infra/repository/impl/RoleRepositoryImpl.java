package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final InMemoryUpmsStore upmsStore;

    public RoleRepositoryImpl(InMemoryUpmsStore upmsStore) {
        this.upmsStore = upmsStore;
    }

    @Override
    public Optional<Role> findRoleById(Long tenantId, Long roleId) {
        return Optional.ofNullable(upmsStore.getRoles().get(InMemoryUpmsStore.roleKey(tenantId, roleId)));
    }

    @Override
    public List<Role> findRolesByUserId(Long tenantId, Long userId) {
        return upmsStore.getUserRoles().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), List.of());
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
                ? new Role(upmsStore.nextRoleId(), role.getTenantId(), role.getCode(), role.getName(), role.getRoleType(),
                role.getDataScopeType(), role.getStatus())
                : role;
        upmsStore.getRoles().put(InMemoryUpmsStore.roleKey(savedRole.getTenantId(), savedRole.getId()), savedRole);
        return savedRole;
    }

    @Override
    public Role updateStatus(Long tenantId, Long roleId, String status) {
        Role currentRole = findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Role updatedRole = new Role(currentRole.getId(), currentRole.getCreatedBy(),
                currentRole.getCreatedAt(), currentRole.getUpdatedBy(), currentRole.getUpdatedAt(),
                currentRole.getTenantId(), currentRole.getCode(), currentRole.getName(),
                currentRole.getRoleType(), currentRole.getDataScopeType(), status);
        upmsStore.getRoles().put(InMemoryUpmsStore.roleKey(tenantId, roleId), updatedRole);
        return updatedRole;
    }

    @Override
    public void deleteRole(Long tenantId, Long roleId) {
        upmsStore.getRoles().remove(InMemoryUpmsStore.roleKey(tenantId, roleId));
        upmsStore.getRoleMenus().remove(InMemoryUpmsStore.roleKey(tenantId, roleId));
        upmsStore.getRoleResources().remove(InMemoryUpmsStore.roleKey(tenantId, roleId));
        upmsStore.getRoleDataScopeTypes().remove(InMemoryUpmsStore.roleKey(tenantId, roleId));
        upmsStore.getRoleDataScopeDepartments().remove(InMemoryUpmsStore.roleKey(tenantId, roleId));
        upmsStore.getUserRoles().replaceAll((key, roles) -> roles.stream()
                .filter(role -> !role.getTenantId().equals(tenantId) || !role.getId().equals(roleId))
                .toList());
    }

    @Override
    public Set<Long> getAssignedMenus(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return upmsStore.getRoleMenus().getOrDefault(InMemoryUpmsStore.roleKey(tenantId, roleId), Set.of());
    }

    @Override
    public Set<Long> assignMenus(Long tenantId, Long roleId, Set<Long> menuIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<Long> safeMenuIds = menuIds == null ? Set.of() : Set.copyOf(menuIds);
        upmsStore.getRoleMenus().put(InMemoryUpmsStore.roleKey(tenantId, roleId), safeMenuIds);
        return safeMenuIds;
    }

    @Override
    public Set<String> getAssignedResources(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return upmsStore.getRoleResources().getOrDefault(InMemoryUpmsStore.roleKey(tenantId, roleId), Set.of());
    }

    @Override
    public Set<String> assignResources(Long tenantId, Long roleId, Set<String> resourceCodes) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<String> safeResourceCodes = resourceCodes == null ? Set.of() : Set.copyOf(resourceCodes);
        upmsStore.getRoleResources().put(InMemoryUpmsStore.roleKey(tenantId, roleId), safeResourceCodes);
        return safeResourceCodes;
    }

    @Override
    public String getAssignedDataScopeType(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return upmsStore.getRoleDataScopeTypes().getOrDefault(InMemoryUpmsStore.roleKey(tenantId, roleId), "SELF");
    }

    @Override
    public Set<Long> getAssignedDataScopeDepartments(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return upmsStore.getRoleDataScopeDepartments()
                .getOrDefault(InMemoryUpmsStore.roleKey(tenantId, roleId), Set.of());
    }

    @Override
    public Set<Long> assignDataScope(Long tenantId, Long roleId, String dataScopeType, Set<Long> departmentIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<Long> safeDepartmentIds = departmentIds == null ? Set.of() : Set.copyOf(departmentIds);
        upmsStore.getRoleDataScopeTypes().put(InMemoryUpmsStore.roleKey(tenantId, roleId), dataScopeType);
        upmsStore.getRoleDataScopeDepartments().put(InMemoryUpmsStore.roleKey(tenantId, roleId), safeDepartmentIds);
        Role currentRole = findRoleById(tenantId, roleId).orElseThrow();
        Role updatedRole = new Role(currentRole.getId(), currentRole.getCreatedBy(), currentRole.getCreatedAt(),
                currentRole.getUpdatedBy(), currentRole.getUpdatedAt(), currentRole.getTenantId(), currentRole.getCode(),
                currentRole.getName(), currentRole.getRoleType(), dataScopeType, currentRole.getStatus());
        upmsStore.getRoles().put(InMemoryUpmsStore.roleKey(tenantId, roleId), updatedRole);
        return safeDepartmentIds;
    }

    private List<Role> filteredRoles(Long tenantId, String code, String name, String roleType, String status) {
        return upmsStore.getRoles().values().stream()
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
}
