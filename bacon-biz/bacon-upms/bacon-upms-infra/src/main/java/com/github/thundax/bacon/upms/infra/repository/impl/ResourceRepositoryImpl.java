package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(UpmsRepositorySupport.class)
public class ResourceRepositoryImpl implements ResourceRepository {

    private final UpmsRepositorySupport support;

    public ResourceRepositoryImpl(UpmsRepositorySupport support) {
        this.support = support;
    }

    @Override
    public Optional<Resource> findById(Long tenantId, Long resourceId) {
        return support.findResourceById(tenantId, resourceId);
    }

    @Override
    public List<Resource> pageResources(Long tenantId, String code, String name, String resourceType, String status,
                                        int pageNo, int pageSize) {
        return support.listResources(tenantId, code, name, resourceType, status, pageNo, pageSize);
    }

    @Override
    public long countResources(Long tenantId, String code, String name, String resourceType, String status) {
        return support.countResources(tenantId, code, name, resourceType, status);
    }

    @Override
    public Resource save(Resource resource) {
        return support.saveResource(resource);
    }

    @Override
    public void delete(Long tenantId, Long resourceId) {
        support.deleteResource(tenantId, resourceId);
    }
}
