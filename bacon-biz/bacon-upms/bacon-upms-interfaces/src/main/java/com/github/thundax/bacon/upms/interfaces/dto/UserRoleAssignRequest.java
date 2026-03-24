package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.List;

public record UserRoleAssignRequest(Long tenantId, List<Long> roleIds) {
}
