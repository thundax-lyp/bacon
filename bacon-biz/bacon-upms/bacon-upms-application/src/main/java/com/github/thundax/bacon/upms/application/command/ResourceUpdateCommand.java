package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;

public record ResourceUpdateCommand(
        ResourceId resourceId,
        ResourceCode code,
        String name,
        ResourceType resourceType,
        String httpMethod,
        String uri,
        ResourceStatus status) {}
