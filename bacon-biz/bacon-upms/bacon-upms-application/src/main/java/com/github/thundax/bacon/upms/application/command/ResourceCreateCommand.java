package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;

public record ResourceCreateCommand(
        ResourceCode code, String name, ResourceType resourceType, String httpMethod, String uri) {}
