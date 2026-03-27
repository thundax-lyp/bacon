package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.upms.api.dto.ResourceDTO;
import com.github.thundax.bacon.upms.api.dto.ResourcePageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.ResourcePageResultDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import org.springframework.stereotype.Service;

@Service
public class ResourceApplicationService {

    private final ResourceRepository resourceRepository;

    public ResourceApplicationService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public ResourcePageResultDTO pageResources(ResourcePageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        return new ResourcePageResultDTO(
                resourceRepository.pageResources(query.getTenantId(), query.getCode(), query.getName(),
                        query.getResourceType(), query.getStatus(), pageNo, pageSize).stream()
                        .map(this::toDto)
                        .toList(),
                resourceRepository.countResources(query.getTenantId(), query.getCode(), query.getName(),
                        query.getResourceType(), query.getStatus()),
                pageNo,
                pageSize
        );
    }

    public ResourceDTO getResourceById(Long tenantId, Long resourceId) {
        return toDto(requireResource(tenantId, resourceId));
    }

    public ResourceDTO createResource(Long tenantId, String code, String name, String resourceType,
                                      String httpMethod, String uri) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(resourceType, "resourceType");
        validateRequired(uri, "uri");
        return toDto(resourceRepository.save(new Resource(null, tenantId, normalize(code), normalize(name),
                normalize(resourceType), normalize(httpMethod), normalize(uri), "ENABLED")));
    }

    public ResourceDTO updateResource(Long tenantId, Long resourceId, String code, String name, String resourceType,
                                      String httpMethod, String uri, String status) {
        Resource currentResource = requireResource(tenantId, resourceId);
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(resourceType, "resourceType");
        validateRequired(uri, "uri");
        return toDto(resourceRepository.save(new Resource(
                currentResource.getId(),
                tenantId,
                normalize(code),
                normalize(name),
                normalize(resourceType),
                normalize(httpMethod),
                normalize(uri),
                normalizeNullable(status, currentResource.getStatus()),
                currentResource.getCreatedBy(),
                currentResource.getCreatedAt(),
                currentResource.getUpdatedBy(),
                currentResource.getUpdatedAt())));
    }

    public void deleteResource(Long tenantId, Long resourceId) {
        requireResource(tenantId, resourceId);
        resourceRepository.delete(tenantId, resourceId);
    }

    private Resource requireResource(Long tenantId, Long resourceId) {
        return resourceRepository.findById(tenantId, resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
    }

    private ResourceDTO toDto(Resource resource) {
        return new ResourceDTO(resource.getId(), resource.getTenantId(), resource.getCode(), resource.getName(),
                resource.getResourceType(), resource.getHttpMethod(), resource.getUri(), resource.getStatus());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : normalize(value);
    }

}
