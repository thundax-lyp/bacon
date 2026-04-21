package com.github.thundax.bacon.upms.interfaces.assembler;

import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.application.codec.ResourceCodeCodec;
import com.github.thundax.bacon.upms.application.codec.ResourceIdCodec;
import com.github.thundax.bacon.upms.application.command.ResourceCreateCommand;
import com.github.thundax.bacon.upms.application.command.ResourceUpdateCommand;
import com.github.thundax.bacon.upms.application.dto.ResourceDTO;
import com.github.thundax.bacon.upms.application.query.ResourcePageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.interfaces.request.ResourceCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.ResourcePageRequest;
import com.github.thundax.bacon.upms.interfaces.request.ResourceUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.ResourcePageResponse;
import com.github.thundax.bacon.upms.interfaces.response.ResourceResponse;

public final class ResourceInterfaceAssembler {

    private ResourceInterfaceAssembler() {}

    public static ResourcePageQuery toPageQuery(ResourcePageRequest request) {
        return new ResourcePageQuery(
                ResourceCodeCodec.toDomain(request.getCode()),
                request.getName(),
                request.getResourceType() == null ? null : ResourceType.from(request.getResourceType()),
                request.getStatus() == null ? null : ResourceStatus.from(request.getStatus()),
                request.getPageNo(),
                request.getPageSize());
    }

    public static ResourceCreateCommand toCreateCommand(ResourceCreateRequest request) {
        return new ResourceCreateCommand(
                ResourceCodeCodec.toDomain(request.code()),
                request.name(),
                request.resourceType() == null ? null : ResourceType.from(request.resourceType()),
                request.httpMethod(),
                request.uri());
    }

    public static ResourceUpdateCommand toUpdateCommand(Long resourceId, ResourceUpdateRequest request) {
        return new ResourceUpdateCommand(
                ResourceIdCodec.toDomain(resourceId),
                ResourceCodeCodec.toDomain(request.code()),
                request.name(),
                request.resourceType() == null ? null : ResourceType.from(request.resourceType()),
                request.httpMethod(),
                request.uri(),
                request.status() == null || request.status().isBlank() ? null : ResourceStatus.from(request.status().trim()));
    }

    public static ResourceId toResourceId(Long resourceId) {
        return ResourceIdCodec.toDomain(resourceId);
    }

    public static ResourceResponse toResponse(ResourceDTO dto) {
        return ResourceResponse.from(dto);
    }

    public static ResourcePageResponse toPageResponse(PageResult<ResourceDTO> pageResult) {
        return ResourcePageResponse.from(pageResult);
    }
}
