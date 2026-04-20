package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.api.dto.ResourceDTO;
import com.github.thundax.bacon.upms.application.assembler.ResourceAssembler;
import com.github.thundax.bacon.upms.application.result.PageResult;
import com.github.thundax.bacon.upms.application.codec.ResourceCodeCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceApplicationService {

    private final Ids ids;
    private final ResourceRepository resourceRepository;

    public ResourceApplicationService(Ids ids, ResourceRepository resourceRepository) {
        this.ids = ids;
        this.resourceRepository = resourceRepository;
    }

    public PageResult<ResourceDTO> page(
            String code,
            String name,
            ResourceType resourceType,
            ResourceStatus status,
            Integer pageNo,
            Integer pageSize) {
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        return new PageResult<>(
                resourceRepository
                        .page(
                                ResourceCodeCodec.toDomain(code),
                                name,
                                resourceType,
                                status,
                                normalizedPageNo,
                                normalizedPageSize)
                        .stream()
                        .map(ResourceAssembler::toDto)
                        .toList(),
                resourceRepository.count(ResourceCodeCodec.toDomain(code), name, resourceType, status),
                normalizedPageNo,
                normalizedPageSize);
    }

    public ResourceDTO getResourceById(ResourceId resourceId) {
        return ResourceAssembler.toDto(requireResource(resourceId));
    }

    @Transactional
    public ResourceDTO createResource(
            String code, String name, ResourceType resourceType, String httpMethod, String uri) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(uri, "uri");
        return ResourceAssembler.toDto(resourceRepository.insert(Resource.create(
                ids.resourceId(),
                ResourceCodeCodec.toDomain(code),
                trimPreservingNull(name),
                resourceType,
                trimPreservingNull(httpMethod),
                trimPreservingNull(uri))));
    }

    @Transactional
    public ResourceDTO updateResource(
            ResourceId resourceId,
            String code,
            String name,
            ResourceType resourceType,
            String httpMethod,
            String uri,
            ResourceStatus status) {
        Resource currentResource = requireResource(resourceId);
        validateRequired(code, "code");
        validateRequired(name, "name");
        if (resourceType == null) {
            throw new BadRequestException("resourceType must not be null");
        }
        validateRequired(uri, "uri");
        currentResource.recodeAs(ResourceCodeCodec.toDomain(code));
        currentResource.rename(trimPreservingNull(name));
        currentResource.classifyAs(resourceType);
        currentResource.exposeEndpoint(trimPreservingNull(httpMethod), trimPreservingNull(uri));
        if (status != null) {
            if (status == ResourceStatus.ENABLED) {
                currentResource.enable();
            } else {
                currentResource.disable();
            }
        }
        return ResourceAssembler.toDto(resourceRepository.update(currentResource));
    }

    @Transactional
    public void deleteResource(ResourceId resourceId) {
        requireResource(resourceId);
        resourceRepository.delete(resourceId);
    }

    private Resource requireResource(ResourceId resourceId) {
        return resourceRepository
                .findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found: " + resourceId));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
    }

    private String trimPreservingNull(String value) {
        return value == null ? null : value.trim();
    }
}
