package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.Set;

public record RoleResourceAssignCommand(RoleId roleId, Set<ResourceCode> resourceCodes) {}
