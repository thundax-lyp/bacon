package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public record RoleMenuAssignRequest(String tenantCode, Set<String> menuIds) {
}
