package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(RolePersistenceSupport.class)
public class RoleRepositoryImpl implements RoleRepository {

    private final RolePersistenceSupport support;
    private final UpmsPermissionCacheSupport cacheSupport;

    public RoleRepositoryImpl(RolePersistenceSupport support, UpmsPermissionCacheSupport cacheSupport) {
        this.support = support;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Optional<Role> findRoleById(TenantId tenantId, Long roleId) {
        return support.findRoleById(tenantId, roleId);
    }

    @Override
    public List<Role> findRolesByUserId(TenantId tenantId, UserId userId) {
        return support.findRolesByUserId(tenantId, userId);
    }

    @Override
    public List<Role> pageRoles(TenantId tenantId, String code, String name, String roleType, String status, int pageNo,
                                int pageSize) {
        return support.listRoles(tenantId, code, name, roleType, status, pageNo, pageSize);
    }

    @Override
    public long countRoles(TenantId tenantId, String code, String name, String roleType, String status) {
        return support.countRoles(tenantId, code, name, roleType, status);
    }

    @Override
    public Role save(Role role) {
        Role savedRole = support.saveRole(role);
        cacheSupport.evictUsersPermission(savedRole.getTenantId(), support.findAssignedUserIds(savedRole.getTenantId(), savedRole.getId()));
        return savedRole;
    }

    @Override
    public Role updateStatus(TenantId tenantId, Long roleId, String status) {
        Role currentRole = findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.saveRole(new Role(
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
                currentRole.getUpdatedAt()));
    }

    @Override
    public void deleteRole(TenantId tenantId, Long roleId) {
        List<UserId> assignedUserIds = support.findAssignedUserIds(tenantId, roleId);
        support.deleteRole(tenantId, roleId);
        cacheSupport.evictUsersPermission(tenantId, assignedUserIds);
    }

    @Override
    public Set<Long> getAssignedMenus(TenantId tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.getAssignedMenuIds(tenantId, roleId);
    }

    @Override
    public Set<Long> assignMenus(TenantId tenantId, Long roleId, Set<Long> menuIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<Long> safeMenuIds = menuIds == null ? Set.of() : Set.copyOf(menuIds);
        support.replaceRoleMenus(tenantId, roleId, safeMenuIds);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, roleId));
        return safeMenuIds;
    }

    @Override
    public Set<String> getAssignedResources(TenantId tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.getAssignedResourceCodes(tenantId, roleId);
    }

    @Override
    public Set<String> assignResources(TenantId tenantId, Long roleId, Set<String> resourceCodes) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<String> safeResourceCodes = resourceCodes == null ? Set.of() : Set.copyOf(resourceCodes);
        support.replaceRoleResources(tenantId, roleId, safeResourceCodes);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, roleId));
        return safeResourceCodes;
    }

    @Override
    public String getAssignedDataScopeType(TenantId tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.getAssignedDataScopeType(tenantId, roleId);
    }

    @Override
    public Set<Long> getAssignedDataScopeDepartments(TenantId tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.getAssignedDataScopeDepartments(tenantId, roleId);
    }

    @Override
    public Set<Long> assignDataScope(TenantId tenantId, Long roleId, String dataScopeType, Set<Long> departmentIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<Long> safeDepartmentIds = departmentIds == null ? Set.of() : Set.copyOf(departmentIds);
        support.replaceRoleDataScope(tenantId, roleId, dataScopeType, safeDepartmentIds);
        Role currentRole = findRoleById(tenantId, roleId).orElseThrow();
        support.saveRole(new Role(currentRole.getId(), currentRole.getTenantId(), currentRole.getCode(), currentRole.getName(),
                currentRole.getRoleType(), dataScopeType, currentRole.getStatus(), currentRole.getCreatedBy(),
                currentRole.getCreatedAt(), currentRole.getUpdatedBy(), currentRole.getUpdatedAt()));
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, roleId));
        return safeDepartmentIds;
    }

    void bindUserRoles(TenantId tenantId, UserId userId, List<Role> assignedRoles) {
        support.replaceUserRoles(tenantId, userId, assignedRoles.stream().map(Role::getId).toList());
        cacheSupport.evictUserPermission(tenantId, userId);
    }

    void clearUserRoles(TenantId tenantId, UserId userId) {
        support.deleteUserRolesByUser(tenantId, userId);
        cacheSupport.evictUserPermission(tenantId, userId);
    }

    void removeMenuFromAssignments(TenantId tenantId, Long menuId) {
        support.removeMenuFromAssignments(tenantId, menuId);
    }
}
