package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public record RoleDataScopeAssignRequest(String tenantCode, String dataScopeType, Set<String> departmentIds) {
}
