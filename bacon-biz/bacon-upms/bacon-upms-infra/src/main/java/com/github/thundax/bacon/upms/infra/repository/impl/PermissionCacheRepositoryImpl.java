package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.repository.PermissionCacheRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class PermissionCacheRepositoryImpl implements PermissionCacheRepository {

    private final UpmsPermissionCacheSupport cacheSupport;

    public PermissionCacheRepositoryImpl(UpmsPermissionCacheSupport cacheSupport) {
        this.cacheSupport = cacheSupport;
    }

    @Override
    public void evictUserPermission(TenantId tenantId, UserId userId) {
        cacheSupport.evictUserPermission(tenantId, userId);
    }
}
