package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public record RoleDataScopeAssignRequest(String tenantNo, String dataScopeType, Set<Long> departmentIds) {
}
