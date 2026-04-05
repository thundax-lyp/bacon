package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.ResourceDTO;
import com.github.thundax.bacon.upms.api.dto.ResourcePageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.ResourcePageResultDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceApplicationService {

    private final ResourceRepository resourceRepository;

    public ResourceApplicationService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public ResourcePageResultDTO pageResources(ResourcePageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        String tenantIdValue = String.valueOf(query.getTenantId().value());
        return new ResourcePageResultDTO(
                resourceRepository.pageResources(query.getTenantId(), query.getCode(), query.getName(),
                        query.getResourceType(), query.getStatus(), pageNo, pageSize).stream()
                        .map(resource -> toDto(resource, tenantIdValue))
                        .toList(),
                resourceRepository.countResources(query.getTenantId(), query.getCode(), query.getName(),
                        query.getResourceType(), query.getStatus()),
                pageNo,
                pageSize
        );
    }

    public ResourceDTO getResourceById(TenantId tenantId, String resourceId) {
        return toDto(requireResource(tenantId, resourceId));
    }

    @Transactional
    public ResourceDTO createResource(TenantId tenantId, String code, String name, String resourceType,
                                      String httpMethod, String uri) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(resourceType, "resourceType");
        validateRequired(uri, "uri");
        return toDto(resourceRepository.save(new Resource(null, tenantId, normalize(code), normalize(name),
                toResourceType(resourceType), normalize(httpMethod), normalize(uri), ResourceStatus.ENABLED)));
    }

    @Transactional
    public ResourceDTO updateResource(TenantId tenantId, String resourceId, String code, String name, String resourceType,
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
                toResourceType(resourceType),
                normalize(httpMethod),
                normalize(uri),
                toResourceStatus(status, currentResource.getStatus()),
                currentResource.getCreatedBy(),
                currentResource.getCreatedAt(),
                currentResource.getUpdatedBy(),
                currentResource.getUpdatedAt())));
    }

    @Transactional
    public void deleteResource(TenantId tenantId, String resourceId) {
        requireResource(tenantId, resourceId);
        resourceRepository.delete(tenantId, ResourceId.of(Long.parseLong(resourceId)));
    }

    private Resource requireResource(TenantId tenantId, String resourceId) {
        return resourceRepository.findById(tenantId, ResourceId.of(Long.parseLong(resourceId)))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
    }

    private ResourceDTO toDto(Resource resource) {
        return toDto(resource, String.valueOf(resource.getTenantId().value()));
    }

    private ResourceDTO toDto(Resource resource, String tenantIdValue) {
        return new ResourceDTO(resource.getId() == null ? null : String.valueOf(resource.getId().value()), tenantIdValue, resource.getCode(),
                resource.getName(),
                resource.getResourceType() == null ? null : resource.getResourceType().value(),
                resource.getHttpMethod(), resource.getUri(),
                resource.getStatus() == null ? null : resource.getStatus().value());
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
        return ResourceType.fromValue(normalize(resourceType).toUpperCase(Locale.ROOT));
    }

    private ResourceStatus toResourceStatus(String status, ResourceStatus defaultValue) {
        if (status == null || status.isBlank()) {
            return defaultValue;
        }
        return ResourceStatus.fromValue(normalize(status).toUpperCase(Locale.ROOT));
    }

}
