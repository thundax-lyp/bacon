package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository {

    Optional<Resource> findById(ResourceId resourceId);

    List<Resource> pageResources(
            String code, String name, ResourceType resourceType, ResourceStatus status, int pageNo, int pageSize);

    long countResources(String code, String name, ResourceType resourceType, ResourceStatus status);

    Resource insert(Resource resource);

    Resource update(Resource resource);

    void delete(ResourceId resourceId);
}
