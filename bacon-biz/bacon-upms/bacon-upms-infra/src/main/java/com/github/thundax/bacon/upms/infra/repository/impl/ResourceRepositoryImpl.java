package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class ResourceRepositoryImpl implements ResourceRepository {

    private final ResourcePersistenceSupport support;
    private final UpmsPermissionCacheSupport cacheSupport;

    public ResourceRepositoryImpl(ResourcePersistenceSupport support, UpmsPermissionCacheSupport cacheSupport) {
        this.support = support;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Optional<Resource> findById(TenantId tenantId, Long resourceId) {
        return support.findResourceById(tenantId, resourceId);
    }

    @Override
    public List<Resource> pageResources(TenantId tenantId, String code, String name, String resourceType, String status,
                                        int pageNo, int pageSize) {
        return support.listResources(tenantId, code, name, resourceType, status, pageNo, pageSize);
    }

    @Override
    public long countResources(TenantId tenantId, String code, String name, String resourceType, String status) {
        return support.countResources(tenantId, code, name, resourceType, status);
    }

    @Override
    public Resource save(Resource resource) {
        Resource savedResource = support.saveResource(resource);
        cacheSupport.evictTenantPermission(savedResource.getTenantId());
        return savedResource;
    }

    @Override
    public void delete(TenantId tenantId, Long resourceId) {
        support.deleteResource(tenantId, resourceId);
        cacheSupport.evictTenantPermission(tenantId);
    }
}
