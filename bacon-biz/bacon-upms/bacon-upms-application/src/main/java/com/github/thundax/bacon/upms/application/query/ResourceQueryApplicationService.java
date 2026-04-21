package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.result.PageResult;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.application.assembler.ResourceAssembler;
import com.github.thundax.bacon.upms.application.dto.ResourceDTO;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import org.springframework.stereotype.Service;

@Service
public class ResourceQueryApplicationService {

    private final ResourceRepository resourceRepository;

    public ResourceQueryApplicationService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public PageResult<ResourceDTO> page(ResourcePageQuery query) {
        int normalizedPageNo = query.getPageNo();
        int normalizedPageSize = query.getPageSize();
        return new PageResult<>(
                resourceRepository
                        .page(
                                query.getCode(),
                                query.getName(),
                                query.getResourceType(),
                                query.getStatus(),
                                normalizedPageNo,
                                normalizedPageSize)
                        .stream()
                        .map(ResourceAssembler::toDto)
                        .toList(),
                resourceRepository.count(query.getCode(), query.getName(), query.getResourceType(), query.getStatus()),
                normalizedPageNo,
                normalizedPageSize);
    }

    public ResourceDTO getById(ResourceId resourceId) {
        return ResourceAssembler.toDto(resourceRepository
                .findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found: " + resourceId)));
    }
}
