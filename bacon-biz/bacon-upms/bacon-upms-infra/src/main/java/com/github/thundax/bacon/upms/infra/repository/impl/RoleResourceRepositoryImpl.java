package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.RoleResourceRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class RoleResourceRepositoryImpl implements RoleResourceRepository {

    private final RolePersistenceSupport roleSupport;
    private final RoleResourceRelPersistenceSupport support;
    private final UpmsPermissionCacheSupport cacheSupport;

    public RoleResourceRepositoryImpl(
            RolePersistenceSupport roleSupport,
            RoleResourceRelPersistenceSupport support,
            UpmsPermissionCacheSupport cacheSupport) {
        this.roleSupport = roleSupport;
        this.support = support;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Set<ResourceCode> findResourceCodes(RoleId roleId) {
        requireRole(roleId);
        return support.findResourceCodes(roleId);
    }

    @Override
    public Set<ResourceCode> updateResourceCodes(RoleId roleId, Set<ResourceCode> resourceCodes) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        requireRole(roleId);
        Set<ResourceCode> safeResourceCodes = resourceCodes == null ? Set.of() : Set.copyOf(resourceCodes);
        support.updateResourceCodes(roleId, safeResourceCodes);
        cacheSupport.evictUsersPermission(tenantId, roleSupport.findAssignedUserIds(roleId));
        return safeResourceCodes;
    }

    private void requireRole(RoleId roleId) {
        roleSupport.findById(roleId).orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value()));
    }
}
