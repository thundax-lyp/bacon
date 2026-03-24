package com.github.thundax.bacon.upms.interfaces.dto;

public record DepartmentCreateRequest(Long tenantId, String code, String name, Long parentId, Long leaderUserId) {
}
