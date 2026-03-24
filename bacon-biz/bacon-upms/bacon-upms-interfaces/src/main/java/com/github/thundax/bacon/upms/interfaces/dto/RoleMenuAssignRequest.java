package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public record RoleMenuAssignRequest(Long tenantId, Set<Long> menuIds) {
}
