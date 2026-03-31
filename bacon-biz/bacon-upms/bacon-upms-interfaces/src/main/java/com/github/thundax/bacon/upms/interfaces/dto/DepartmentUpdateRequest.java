package com.github.thundax.bacon.upms.interfaces.dto;

public record DepartmentUpdateRequest(String tenantId, String code, String name, Long parentId, String leaderUserId) {
}
