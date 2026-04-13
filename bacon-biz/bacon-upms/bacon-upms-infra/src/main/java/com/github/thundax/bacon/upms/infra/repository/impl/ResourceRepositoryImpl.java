package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.ResourceId;
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
    private final Ids ids;

    public ResourceRepositoryImpl(
            ResourcePersistenceSupport support, UpmsPermissionCacheSupport cacheSupport, Ids ids) {
        this.support = support;
        this.cacheSupport = cacheSupport;
        this.ids = ids;
    }

    @Override
    public Optional<Resource> findById(ResourceId resourceId) {
        return support.findResourceById(resourceId);
    }

    @Override
    public List<Resource> pageResources(
            String code, String name, String resourceType, String status, int pageNo, int pageSize) {
        return support.listResources(code, name, resourceType, status, pageNo, pageSize);
    }

    @Override
    public long countResources(String code, String name, String resourceType, String status) {
        return support.countResources(code, name, resourceType, status);
    }

    @Override
    public Resource save(Resource resource) {
        TenantId tenantId = requireTenantId();
        Resource resourceToSave = resource.getId() == null
                ? Resource.create(
                        ids.resourceId(),
                        resource.getCode(),
                        resource.getName(),
                        resource.getResourceType(),
                        resource.getHttpMethod(),
                        resource.getUri(),
                        resource.getStatus())
                : resource;
        Resource savedResource = support.saveResource(resourceToSave);
        cacheSupport.evictTenantPermission(tenantId);
        return savedResource;
    }

    @Override
    public void delete(ResourceId resourceId) {
        TenantId tenantId = requireTenantId();
        support.deleteResource(resourceId);
        cacheSupport.evictTenantPermission(tenantId);
    }

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
