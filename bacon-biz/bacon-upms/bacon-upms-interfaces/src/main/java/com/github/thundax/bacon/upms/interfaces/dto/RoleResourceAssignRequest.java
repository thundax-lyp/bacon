package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public record RoleResourceAssignRequest(String tenantId, Set<String> resourceCodes) {
}
