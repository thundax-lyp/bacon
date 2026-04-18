package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.RoleDataScopeRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class RoleDataScopeRepositoryImpl implements RoleDataScopeRepository {

    private final RolePersistenceSupport roleSupport;
    private final RoleDataScopePersistenceSupport support;
    private final UpmsPermissionCacheSupport cacheSupport;

    public RoleDataScopeRepositoryImpl(
            RolePersistenceSupport roleSupport,
            RoleDataScopePersistenceSupport support,
            UpmsPermissionCacheSupport cacheSupport) {
        this.roleSupport = roleSupport;
        this.support = support;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public RoleDataScopeType findDataScopeType(RoleId roleId) {
        requireRole(roleId);
        return support.findDataScopeType(roleId);
    }

    @Override
    public Set<DepartmentId> findDataScopeDepartmentIds(RoleId roleId) {
        requireRole(roleId);
        return support.findDataScopeDepartmentIds(roleId);
    }

    @Override
    public Set<DepartmentId> updateDataScope(
            RoleId roleId, RoleDataScopeType dataScopeType, Set<DepartmentId> departmentIds) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        Role role = requireRole(roleId);
        Set<DepartmentId> normalizedDepartmentIds = role.assignDataScope(dataScopeType, departmentIds);
        support.updateDataScope(roleId, role.getDataScopeType(), normalizedDepartmentIds);
        roleSupport.update(role);
        cacheSupport.evictUsersPermission(tenantId, roleSupport.findAssignedUserIds(roleId));
        return normalizedDepartmentIds;
    }

    private Role requireRole(RoleId roleId) {
        return roleSupport.findById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
    }
}
