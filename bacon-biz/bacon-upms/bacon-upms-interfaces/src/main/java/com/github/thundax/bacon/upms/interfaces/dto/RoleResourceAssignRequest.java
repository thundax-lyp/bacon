package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public record RoleResourceAssignRequest(Long tenantId, Set<String> resourceCodes) {
}
