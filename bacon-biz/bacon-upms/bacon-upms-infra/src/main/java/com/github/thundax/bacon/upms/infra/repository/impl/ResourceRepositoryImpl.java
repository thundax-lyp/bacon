package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
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
    public Optional<Resource> findById(ResourceId resourceId) {
        return support.findResourceById(resourceId);
    }

    @Override
    public List<Resource> page(
            ResourceCode code, String name, ResourceType resourceType, ResourceStatus status, int pageNo, int pageSize) {
        return support.listResources(code, name, resourceType, status, pageNo, pageSize);
    }

    @Override
    public long count(ResourceCode code, String name, ResourceType resourceType, ResourceStatus status) {
        return support.count(code, name, resourceType, status);
    }

    @Override
    public Resource insert(Resource resource) {
        TenantId tenantId = requireTenantId();
        Resource savedResource = support.insertResource(resource);
        cacheSupport.evictTenantPermission(tenantId);
        return savedResource;
    }

    @Override
    public Resource update(Resource resource) {
        TenantId tenantId = requireTenantId();
        Resource savedResource = support.updateResource(resource);
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
