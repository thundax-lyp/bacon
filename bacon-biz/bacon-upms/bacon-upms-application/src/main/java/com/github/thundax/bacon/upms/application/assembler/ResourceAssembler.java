package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.upms.api.dto.ResourceDTO;
import com.github.thundax.bacon.upms.application.codec.ResourceIdCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;

public final class ResourceAssembler {

    private ResourceAssembler() {}

    public static ResourceDTO toDto(Resource resource) {
        return new ResourceDTO(
                ResourceIdCodec.toValue(resource.getId()),
                resource.getCode(),
                resource.getName(),
                resource.getResourceType() == null
                        ? null
                        : resource.getResourceType().value(),
                resource.getHttpMethod(),
                resource.getUri(),
                resource.getStatus() == null ? null : resource.getStatus().value());
    }
}
