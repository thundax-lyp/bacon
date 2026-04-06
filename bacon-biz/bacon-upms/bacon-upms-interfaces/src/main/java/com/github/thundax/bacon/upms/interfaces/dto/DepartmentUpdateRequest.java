package com.github.thundax.bacon.upms.interfaces.dto;

public record DepartmentUpdateRequest(String tenantCode, String code, String name, String parentId, String leaderUserId,
                                      Integer sort) {
}
