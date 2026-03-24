package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.entity.Resource;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository {

    Optional<Resource> findById(Long tenantId, Long resourceId);

    List<Resource> pageResources(Long tenantId, String code, String name, String resourceType, String status,
                                 int pageNo, int pageSize);

    long countResources(Long tenantId, String code, String name, String resourceType, String status);

    Resource save(Resource resource);

    void delete(Long tenantId, Long resourceId);
}
