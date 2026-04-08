package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.List;

public record UserRoleAssignRequest(String tenantCode, List<Long> roleIds) {
}
