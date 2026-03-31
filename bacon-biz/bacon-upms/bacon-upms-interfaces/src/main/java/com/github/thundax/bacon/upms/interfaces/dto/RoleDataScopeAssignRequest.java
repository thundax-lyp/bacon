package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public record RoleDataScopeAssignRequest(String tenantId, String dataScopeType, Set<Long> departmentIds) {
}
