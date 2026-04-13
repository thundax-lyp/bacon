package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.ResourceDTO;
import com.github.thundax.bacon.upms.api.dto.ResourcePageQueryDTO;
import com.github.thundax.bacon.upms.application.assembler.ResourceAssembler;
import com.github.thundax.bacon.upms.application.codec.ResourceIdCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import java.util.Locale;
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

    public PageResultDTO<ResourceDTO> pageResources(ResourcePageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        return new PageResultDTO<>(
                resourceRepository
                        .pageResources(
                                query.getCode(),
                                query.getName(),
                                query.getResourceType(),
                                query.getStatus(),
                                pageNo,
                                pageSize)
                        .stream()
                        .map(this::toDto)
                        .toList(),
                resourceRepository.countResources(
                        query.getCode(), query.getName(), query.getResourceType(), query.getStatus()),
                pageNo,
                pageSize);
    }

    public ResourceDTO getResourceById(String resourceId) {
        return toDto(requireResource(resourceId));
    }

    @Transactional
    public ResourceDTO createResource(String code, String name, String resourceType, String httpMethod, String uri) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(resourceType, "resourceType");
        validateRequired(uri, "uri");
        return toDto(resourceRepository.save(Resource.create(
                ids.resourceId(),
                normalize(code),
                normalize(name),
                toResourceType(resourceType),
                normalize(httpMethod),
                normalize(uri),
                ResourceStatus.ENABLED)));
    }

    @Transactional
    public ResourceDTO updateResource(
            String resourceId,
            String code,
            String name,
            String resourceType,
            String httpMethod,
            String uri,
            String status) {
        Resource currentResource = requireResource(resourceId);
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(resourceType, "resourceType");
        validateRequired(uri, "uri");
        return toDto(resourceRepository.save(currentResource.update(
                normalize(code),
                normalize(name),
                toResourceType(resourceType),
                normalize(httpMethod),
                normalize(uri),
                toResourceStatus(status, currentResource.getStatus()))));
    }

    @Transactional
    public void deleteResource(String resourceId) {
        requireResource(resourceId);
        resourceRepository.delete(ResourceIdCodec.toDomain(Long.parseLong(resourceId)));
    }

    private Resource requireResource(String resourceId) {
        return resourceRepository
                .findById(ResourceIdCodec.toDomain(Long.parseLong(resourceId)))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
    }

    private ResourceDTO toDto(Resource resource) {
        return ResourceAssembler.toDto(resource);
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private ResourceType toResourceType(String resourceType) {
        validateRequired(resourceType, "resourceType");
        return ResourceType.from(normalize(resourceType).toUpperCase(Locale.ROOT));
    }

    private ResourceStatus toResourceStatus(String status, ResourceStatus defaultValue) {
        if (status == null || status.isBlank()) {
            return defaultValue;
        }
        return ResourceStatus.from(normalize(status).toUpperCase(Locale.ROOT));
    }
}
