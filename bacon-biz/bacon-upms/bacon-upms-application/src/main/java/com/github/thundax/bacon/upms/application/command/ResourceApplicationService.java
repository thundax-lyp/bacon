package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.ResourceId;
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
                        .map(ResourceAssembler::toDto)
                        .toList(),
                resourceRepository.countResources(
                        query.getCode(), query.getName(), query.getResourceType(), query.getStatus()),
                pageNo,
                pageSize);
    }

    public ResourceDTO getResourceById(ResourceId resourceId) {
        return ResourceAssembler.toDto(requireResource(resourceId));
    }

    @Transactional
    public ResourceDTO createResource(String code, String name, ResourceType resourceType, String httpMethod, String uri) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(uri, "uri");
        return ResourceAssembler.toDto(resourceRepository.insert(Resource.create(
                ids.resourceId(),
                normalize(code),
                normalize(name),
                resourceType,
                normalize(httpMethod),
                normalize(uri),
                ResourceStatus.ENABLED)));
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
            throw new IllegalArgumentException("resourceType must not be null");
        }
        validateRequired(uri, "uri");
        return ResourceAssembler.toDto(resourceRepository.update(currentResource.update(
                normalize(code),
                normalize(name),
                resourceType,
                normalize(httpMethod),
                normalize(uri),
                status == null ? currentResource.getStatus() : status)));
    }

    @Transactional
    public void deleteResource(ResourceId resourceId) {
        requireResource(resourceId);
        resourceRepository.delete(resourceId);
    }

    private Resource requireResource(ResourceId resourceId) {
        return resourceRepository
                .findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

}
