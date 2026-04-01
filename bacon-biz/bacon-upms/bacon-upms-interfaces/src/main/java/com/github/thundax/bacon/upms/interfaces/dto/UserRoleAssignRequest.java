package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.List;

public record UserRoleAssignRequest(String tenantId, List<String> roleIds) {
}
