package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.ResourceDTO;

/**
 * 资源查询响应对象。
 */
public record ResourceResponse(
        /** 资源主键。 */
        Long id,
        /** 所属租户编号。 */
        Long tenantId,
        /** 资源编码。 */
        String code,
        /** 资源名称。 */
        String name,
        /** 资源类型。 */
        String resourceType,
        /** HTTP 方法。 */
        String httpMethod,
        /** 资源 URI。 */
        String uri,
        /** 资源状态。 */
        String status) {

    public static ResourceResponse from(ResourceDTO dto) {
        return new ResourceResponse(
                dto.getId(),
                dto.getTenantId(),
                dto.getCode(),
                dto.getName(),
                dto.getResourceType(),
                dto.getHttpMethod(),
                dto.getUri(),
                dto.getStatus());
    }
}
