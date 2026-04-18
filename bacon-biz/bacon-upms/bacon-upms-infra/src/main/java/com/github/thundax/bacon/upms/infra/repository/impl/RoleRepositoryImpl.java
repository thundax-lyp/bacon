package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
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
    private final RoleDataScopePersistenceSupport dataScopeSupport;
    private final UpmsPermissionCacheSupport cacheSupport;

    public RoleRepositoryImpl(
            RolePersistenceSupport support,
            RoleDataScopePersistenceSupport dataScopeSupport,
            UpmsPermissionCacheSupport cacheSupport) {
        this.support = support;
        this.dataScopeSupport = dataScopeSupport;
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
        dataScopeSupport.updateDataScope(savedRole.getId(), savedRole.getDataScopeType(), Set.of());
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(savedRole.getId()));
        return savedRole;
    }

    @Override
    public Role update(Role role) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        Role savedRole = support.update(role);
        dataScopeSupport.updateDataScope(
                savedRole.getId(),
                savedRole.getDataScopeType(),
                dataScopeSupport.findDataScopeDepartmentIds(savedRole.getId()));
        cacheSupport.evictUsersPermission(tenantId, support.findAssignedUserIds(savedRole.getId()));
        return savedRole;
    }

    @Override
    public void delete(RoleId roleId) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        List<UserId> assignedUserIds = support.findAssignedUserIds(roleId);
        dataScopeSupport.delete(roleId);
        support.delete(roleId);
        cacheSupport.evictUsersPermission(tenantId, assignedUserIds);
    }
}
