package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public record RoleResourceAssignRequest(String tenantNo, Set<String> resourceCodes) {
}
