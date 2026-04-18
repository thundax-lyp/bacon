package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.RoleMenuRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class RoleMenuRepositoryImpl implements RoleMenuRepository {

    private final RolePersistenceSupport roleSupport;
    private final RoleMenuPersistenceSupport support;
    private final UpmsPermissionCacheSupport cacheSupport;

    public RoleMenuRepositoryImpl(
            RolePersistenceSupport roleSupport,
            RoleMenuPersistenceSupport support,
            UpmsPermissionCacheSupport cacheSupport) {
        this.roleSupport = roleSupport;
        this.support = support;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Set<MenuId> findMenuIds(RoleId roleId) {
        requireRole(roleId);
        return support.findMenuIds(roleId);
    }

    @Override
    public Set<MenuId> updateMenuIds(RoleId roleId, Set<MenuId> menuIds) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        requireRole(roleId);
        Set<MenuId> safeMenuIds = menuIds == null ? Set.of() : Set.copyOf(menuIds);
        support.updateMenuIds(roleId, safeMenuIds);
        cacheSupport.evictUsersPermission(tenantId, roleSupport.findAssignedUserIds(roleId));
        return safeMenuIds;
    }

    private void requireRole(RoleId roleId) {
        roleSupport.findById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
    }
}
