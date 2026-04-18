package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
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
    public Optional<Role> findById(RoleId roleId) {
        return support.findById(roleId);
    }

    @Override
    public List<Role> findByUserId(UserId userId) {
        return support.findByUserId(userId);
    }

    @Override
    public List<Role> page(
            RoleCode code, String name, RoleType roleType, RoleStatus status, int pageNo, int pageSize) {
        return support.page(code, name, roleType, status, pageNo, pageSize);
    }

    @Override
    public long count(RoleCode code, String name, RoleType roleType, RoleStatus status) {
        return support.count(code, name, roleType, status);
    }

    @Override
    public Role insert(Role role) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        Role savedRole = support.insert(role);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(savedRole.getId()));
        return savedRole;
    }

    @Override
    public Role update(Role role) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        Role savedRole = support.update(role);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(savedRole.getId()));
        return savedRole;
    }

    @Override
    public void delete(RoleId roleId) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        List<UserId> assignedUserIds = support.findAssignedUserIds(roleId);
        support.delete(roleId);
        cacheSupport.evictUsersPermission(tenantId, assignedUserIds);
    }

    @Override
    public Set<MenuId> findMenuIds(RoleId roleId) {
        findById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        return support.findMenuIds(roleId);
    }

    @Override
    public Set<MenuId> updateMenuIds(RoleId roleId, Set<MenuId> menuIds) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        findById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        Set<MenuId> safeMenuIds = menuIds == null ? Set.of() : Set.copyOf(menuIds);
        support.replaceRoleMenus(roleId, safeMenuIds);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(roleId));
        return safeMenuIds;
    }

    @Override
    public Set<ResourceCode> findResourceCodes(RoleId roleId) {
        findById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        return support.findResourceCodes(roleId);
    }

    @Override
    public Set<ResourceCode> updateResourceCodes(RoleId roleId, Set<ResourceCode> resourceCodes) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        findById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        Set<ResourceCode> safeResourceCodes = resourceCodes == null ? Set.of() : Set.copyOf(resourceCodes);
        support.replaceRoleResources(roleId, safeResourceCodes);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(roleId));
        return safeResourceCodes;
    }

    @Override
    public RoleDataScopeAssignment findDataScope(RoleId roleId) {
        findById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        return support.findDataScope(roleId);
    }

    @Override
    public RoleDataScopeAssignment updateDataScope(RoleId roleId, RoleDataScopeAssignment assignment) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        Role currentRole = findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
        Set<DepartmentId> assignedDepartmentIds =
                currentRole.assignDataScope(assignment.dataScopeType(), assignment.departmentIds());
        RoleDataScopeAssignment normalizedAssignment =
                RoleDataScopeAssignment.of(currentRole.getDataScopeType(), assignedDepartmentIds);
        support.replaceRoleDataScope(
                roleId, normalizedAssignment.dataScopeType(), normalizedAssignment.departmentIds());
        support.update(currentRole);
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(roleId));
        return normalizedAssignment;
    }

}
