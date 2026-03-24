package com.github.thundax.bacon.upms.interfaces.dto;

public record DepartmentUpdateRequest(Long tenantId, String code, String name, Long parentId, Long leaderUserId) {
}
