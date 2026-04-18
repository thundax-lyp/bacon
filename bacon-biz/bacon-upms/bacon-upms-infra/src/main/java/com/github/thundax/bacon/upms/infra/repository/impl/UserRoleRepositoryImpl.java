package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRoleRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleRelPersistenceSupport support;
    private final RoleRepository roleRepository;
    private final UpmsPermissionCacheSupport cacheSupport;

    public UserRoleRepositoryImpl(
            UserRoleRelPersistenceSupport support,
            RoleRepository roleRepository,
            UpmsPermissionCacheSupport cacheSupport) {
        this.support = support;
        this.roleRepository = roleRepository;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public List<Role> updateRoleIds(UserId userId, List<RoleId> roleIds) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        List<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository
                        .findById(roleId)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value())))
                .toList();
        support.updateRoleIds(userId, roles.stream().map(Role::getId).toList());
        cacheSupport.evictUserPermission(tenantId, userId);
        return roles;
    }
}
