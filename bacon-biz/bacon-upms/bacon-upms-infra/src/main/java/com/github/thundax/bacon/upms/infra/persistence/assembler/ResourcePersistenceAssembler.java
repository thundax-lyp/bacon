package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.ResourceDO;

public final class ResourcePersistenceAssembler {

    private ResourcePersistenceAssembler() {}

    public static ResourceDO toDataObject(Resource resource) {
        return new ResourceDO(
                resource.getId() == null ? null : resource.getId().value(),
                BaconContextHolder.requireTenantId(),
                resource.getCode() == null ? null : resource.getCode().value(),
                resource.getName(),
                resource.getResourceType() == null
                        ? null
                        : resource.getResourceType().value(),
                resource.getHttpMethod(),
                resource.getUri(),
                resource.getStatus() == null ? null : resource.getStatus().value());
    }

    public static Resource toDomain(ResourceDO dataObject) {
        ResourceId resourceId = dataObject.getId() == null ? null : ResourceId.of(dataObject.getId());
        return Resource.reconstruct(
                resourceId,
                dataObject.getCode() == null ? null : ResourceCode.of(dataObject.getCode()),
                dataObject.getName(),
                ResourceType.from(dataObject.getResourceType()),
                dataObject.getHttpMethod(),
                dataObject.getUri(),
                ResourceStatus.from(dataObject.getStatus()));
    }
}
