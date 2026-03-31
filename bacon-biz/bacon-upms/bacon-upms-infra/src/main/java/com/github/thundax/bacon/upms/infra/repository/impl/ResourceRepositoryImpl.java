package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceRepositoryImpl implements ResourceRepository {

    private final Map<String, Resource> resources = new ConcurrentHashMap<>();
    private final AtomicLong resourceIdSequence = new AtomicLong(7002L);

    @Override
    public Optional<Resource> findById(Long tenantId, Long resourceId) {
        return Optional.ofNullable(resources.get(UpmsRepositoryHelper.resourceKey(tenantId, resourceId)));
    }

    @Override
    public List<Resource> pageResources(Long tenantId, String code, String name, String resourceType, String status,
                                        int pageNo, int pageSize) {
        return filteredResources(tenantId, code, name, resourceType, status).stream()
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    @Override
    public long countResources(Long tenantId, String code, String name, String resourceType, String status) {
        return filteredResources(tenantId, code, name, resourceType, status).size();
    }

    @Override
    public Resource save(Resource resource) {
        Long resourceId = resource.getId() == null ? resourceIdSequence.getAndIncrement() : resource.getId();
        Resource savedResource = resource.getId() == null
                ? new Resource(resourceId, resource.getTenantId(), resource.getCode(), resource.getName(),
                resource.getResourceType(), resource.getHttpMethod(), resource.getUri(), resource.getStatus())
                : resource;
        resources.put(UpmsRepositoryHelper.resourceKey(savedResource.getTenantId(), savedResource.getId()), savedResource);
        return savedResource;
    }

    @Override
    public void delete(Long tenantId, Long resourceId) {
        resources.remove(UpmsRepositoryHelper.resourceKey(tenantId, resourceId));
    }

    private List<Resource> filteredResources(Long tenantId, String code, String name, String resourceType,
                                             String status) {
        return resources.values().stream()
                .filter(resource -> tenantId == null || tenantId.equals(resource.getTenantId()))
                .filter(resource -> code == null || resource.getCode().contains(code))
                .filter(resource -> name == null || resource.getName().contains(name))
                .filter(resource -> resourceType == null || resourceType.equalsIgnoreCase(resource.getResourceType()))
                .filter(resource -> status == null || status.equalsIgnoreCase(resource.getStatus()))
                .sorted(Comparator.comparing(Resource::getId))
                .toList();
    }
}
