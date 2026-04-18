package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleDataScopeAssignment;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class RoleRepositoryImpl implements RoleRepository {

    private final RolePersistenceSupport support;
    private final UpmsPermissionCacheSupport cacheSupport;

    public RoleRepositoryImpl(RolePersistenceSupport support, UpmsPermissionCacheSupport cacheSupport) {
        this.support = support;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Optional<Role> findRoleById(RoleId roleId) {
        return support.findRoleById(roleId);
    }

    @Override
    public List<Role> findRolesByUserId(UserId userId) {
        return support.findRolesByUserId(userId);
    }

    @Override
    public List<Role> pageRoles(
            RoleCode code, String name, RoleType roleType, RoleStatus status, int pageNo, int pageSize) {
        return support.listRoles(code, name, roleType, status, pageNo, pageSize);
    }

    @Override
    public long countRoles(RoleCode code, String name, RoleType roleType, RoleStatus status) {
        return support.countRoles(code, name, roleType, status);
    }

    @Override
    public Role insert(Role role) {
        TenantId tenantId = requireTenantId();
        Role savedRole = support.insertRole(role);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, savedRole.getId()));
        return savedRole;
    }

    @Override
    public Role update(Role role) {
        TenantId tenantId = requireTenantId();
        Role savedRole = support.updateRole(role);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, savedRole.getId()));
        return savedRole;
    }

    @Override
    public void deleteRole(RoleId roleId) {
        TenantId tenantId = requireTenantId();
        List<UserId> assignedUserIds = support.findAssignedUserIds(tenantId, roleId);
        support.deleteRole(roleId);
        cacheSupport.evictUsersPermission(tenantId, assignedUserIds);
    }

    @Override
    public Set<MenuId> findMenuIds(RoleId roleId) {
        findRoleById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        return support.getAssignedMenuIds(roleId);
    }

    @Override
    public Set<MenuId> updateMenuIds(RoleId roleId, Set<MenuId> menuIds) {
        TenantId tenantId = requireTenantId();
        findRoleById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        Set<MenuId> safeMenuIds = menuIds == null ? Set.of() : Set.copyOf(menuIds);
        support.replaceRoleMenus(roleId, safeMenuIds);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, roleId));
        return safeMenuIds;
    }

    @Override
    public Set<ResourceCode> findResourceCodes(RoleId roleId) {
        findRoleById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        return support.getAssignedResourceCodes(roleId);
    }

    @Override
    public Set<ResourceCode> updateResourceCodes(RoleId roleId, Set<ResourceCode> resourceCodes) {
        TenantId tenantId = requireTenantId();
        findRoleById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        Set<ResourceCode> safeResourceCodes = resourceCodes == null ? Set.of() : Set.copyOf(resourceCodes);
        support.replaceRoleResources(roleId, safeResourceCodes);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, roleId));
        return safeResourceCodes;
    }

    @Override
    public RoleDataScopeAssignment findDataScope(RoleId roleId) {
        findRoleById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        return support.findAssignedDataScope(roleId);
    }

    @Override
    public RoleDataScopeAssignment updateDataScope(RoleId roleId, RoleDataScopeAssignment assignment) {
        TenantId tenantId = requireTenantId();
        Role currentRole = findRoleById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        Set<DepartmentId> assignedDepartmentIds =
                currentRole.assignDataScope(assignment.dataScopeType(), assignment.departmentIds());
        RoleDataScopeAssignment normalizedAssignment =
                RoleDataScopeAssignment.of(currentRole.getDataScopeType(), assignedDepartmentIds);
        support.replaceRoleDataScope(
                roleId, normalizedAssignment.dataScopeType(), normalizedAssignment.departmentIds());
        support.updateRole(currentRole);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(tenantId, roleId));
        return normalizedAssignment;
    }

    void bindUserRoles(TenantId tenantId, UserId userId, List<Role> assignedRoles) {
        support.replaceUserRoles(userId, assignedRoles.stream().map(Role::getId).toList());
        cacheSupport.evictUserPermission(tenantId, userId);
    }

    void clearUserRoles(TenantId tenantId, UserId userId) {
        support.deleteUserRolesByUser(userId);
        cacheSupport.evictUserPermission(tenantId, userId);
    }

    void removeMenuFromAssignments(MenuId menuId) {
        support.removeMenuFromAssignments(menuId);
    }

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
