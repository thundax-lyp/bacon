package com.github.thundax.bacon.upms.interfaces.dto;

public record DepartmentCreateRequest(String tenantId, String code, String name, String parentId, String leaderUserId,
                                      Integer sort) {
}
