package com.github.thundax.bacon.upms.interfaces.dto;

public record PostCreateRequest(String tenantId, String code, String name, String departmentId) {
}
