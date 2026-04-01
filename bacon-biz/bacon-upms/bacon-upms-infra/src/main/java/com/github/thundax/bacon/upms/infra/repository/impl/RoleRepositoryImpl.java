package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.RoleId;
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
    private final Ids ids;

    public RoleRepositoryImpl(RolePersistenceSupport support, UpmsPermissionCacheSupport cacheSupport, Ids ids) {
        this.support = support;
        this.cacheSupport = cacheSupport;
        this.ids = ids;
    }

    @Override
    public Optional<Role> findRoleById(TenantId tenantId, RoleId roleId) {
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
        Role roleToSave = role.getId() == null
                ? new Role(ids.roleId(), role.getTenantId(), role.getCode(), role.getName(), role.getRoleType(),
                role.getDataScopeType(), role.getStatus(), role.getCreatedBy(), role.getCreatedAt(),
                role.getUpdatedBy(), role.getUpdatedAt())
                : role;
        Role savedRole = support.saveRole(roleToSave);
        cacheSupport.evictUsersPermission(savedRole.getTenantId(), support.findAssignedUserIds(savedRole.getTenantId(), savedRole.getId()));
        return savedRole;
    }

    @Override
    public Role updateStatus(TenantId tenantId, RoleId roleId, String status) {
        Role currentRole = findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value()));
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
    public void deleteRole(TenantId tenantId, RoleId roleId) {
        List<UserId> assignedUserIds = support.findAssignedUserIds(tenantId, roleId);
        support.deleteRole(tenantId, roleId);
        cacheSupport.evictUsersPermission(tenantId, assignedUserIds);
    }

    @Override
    public Set<Long> getAssignedMenus(TenantId tenantId, RoleId roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value()));
        return support.getAssignedMenuIds(tenantId, roleId);
    }

    @Override
    public Set<Long> assignMenus(TenantId tenantId, RoleId roleId, Set<Long> menuIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value()));
        Set<Long> safeMenuIds = menuIds == null ? Set.of() : Set.copyOf(menuIds);
        support.replaceRoleMenus(tenantId, roleId, safeMenuIds);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, roleId));
        return safeMenuIds;
    }

    @Override
    public Set<String> getAssignedResources(TenantId tenantId, RoleId roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value()));
        return support.getAssignedResourceCodes(tenantId, roleId);
    }

    @Override
    public Set<String> assignResources(TenantId tenantId, RoleId roleId, Set<String> resourceCodes) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value()));
        Set<String> safeResourceCodes = resourceCodes == null ? Set.of() : Set.copyOf(resourceCodes);
        support.replaceRoleResources(tenantId, roleId, safeResourceCodes);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, roleId));
        return safeResourceCodes;
    }

    @Override
    public String getAssignedDataScopeType(TenantId tenantId, RoleId roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value()));
        return support.getAssignedDataScopeType(tenantId, roleId);
    }

    @Override
    public Set<DepartmentId> getAssignedDataScopeDepartments(TenantId tenantId, RoleId roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value()));
        return support.getAssignedDataScopeDepartments(tenantId, roleId);
    }

    @Override
    public Set<DepartmentId> assignDataScope(TenantId tenantId, RoleId roleId, String dataScopeType, Set<DepartmentId> departmentIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value()));
        Set<DepartmentId> safeDepartmentIds = departmentIds == null ? Set.of() : Set.copyOf(departmentIds);
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
