package com.github.thundax.bacon.upms.interfaces.dto;

public record DepartmentCreateRequest(String tenantNo, String code, String name, Long parentId, String leaderUserId) {
}
