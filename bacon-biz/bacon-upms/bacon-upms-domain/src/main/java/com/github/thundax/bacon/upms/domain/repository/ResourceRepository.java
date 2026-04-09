package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository {

    Optional<Resource> findById(TenantId tenantId, ResourceId resourceId);

    List<Resource> pageResources(
            TenantId tenantId, String code, String name, String resourceType, String status, int pageNo, int pageSize);

    long countResources(TenantId tenantId, String code, String name, String resourceType, String status);

    Resource save(Resource resource);

    void delete(TenantId tenantId, ResourceId resourceId);
}
