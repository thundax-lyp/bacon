package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.ResourceDTO;

public record ResourceResponse(Long id, Long tenantId, String code, String name, String resourceType,
                               String httpMethod, String uri, String status) {

    public static ResourceResponse from(ResourceDTO dto) {
        return new ResourceResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(),
                dto.getResourceType(), dto.getHttpMethod(), dto.getUri(), dto.getStatus());
    }
}
