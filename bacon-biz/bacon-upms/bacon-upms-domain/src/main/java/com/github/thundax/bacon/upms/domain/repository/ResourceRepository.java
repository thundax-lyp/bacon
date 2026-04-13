package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository {

    Optional<Resource> findById(ResourceId resourceId);

    List<Resource> pageResources(
            String code, String name, String resourceType, String status, int pageNo, int pageSize);

    long countResources(String code, String name, String resourceType, String status);

    Resource save(Resource resource);

    void delete(ResourceId resourceId);
}
