package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.application.assembler.ResourceAssembler;
import com.github.thundax.bacon.upms.application.dto.ResourceDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceCommandApplicationService {

    private final Ids ids;
    private final ResourceRepository resourceRepository;

    public ResourceCommandApplicationService(Ids ids, ResourceRepository resourceRepository) {
        this.ids = ids;
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public ResourceDTO create(ResourceCreateCommand command) {
        validateRequired(command.name(), "name");
        validateRequired(command.uri(), "uri");
        if (command.code() == null) {
            throw new BadRequestException("code must not be null");
        }
        if (command.resourceType() == null) {
            throw new BadRequestException("resourceType must not be null");
        }
        return ResourceAssembler.toDto(resourceRepository.insert(Resource.create(
                ids.resourceId(),
                command.code(),
                trimPreservingNull(command.name()),
                command.resourceType(),
                trimPreservingNull(command.httpMethod()),
                trimPreservingNull(command.uri()))));
    }

    @Transactional
    public ResourceDTO update(ResourceUpdateCommand command) {
        Resource currentResource = requireResource(command.resourceId());
        validateRequired(command.name(), "name");
        validateRequired(command.uri(), "uri");
        if (command.code() == null) {
            throw new BadRequestException("code must not be null");
        }
        if (command.resourceType() == null) {
            throw new BadRequestException("resourceType must not be null");
        }
        currentResource.recodeAs(command.code());
        currentResource.rename(trimPreservingNull(command.name()));
        currentResource.classifyAs(command.resourceType());
        currentResource.exposeEndpoint(trimPreservingNull(command.httpMethod()), trimPreservingNull(command.uri()));
        if (command.status() != null) {
            updateStatus(currentResource, command.status());
        }
        return ResourceAssembler.toDto(resourceRepository.update(currentResource));
    }

    @Transactional
    public void delete(ResourceId resourceId) {
        requireResource(resourceId);
        resourceRepository.delete(resourceId);
    }

    private void updateStatus(Resource resource, ResourceStatus status) {
        if (status == ResourceStatus.ENABLED) {
            resource.enable();
        } else {
            resource.disable();
        }
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
