package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public record RoleMenuAssignRequest(String tenantNo, Set<Long> menuIds) {
}
