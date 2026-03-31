package com.github.thundax.bacon.upms.interfaces.dto;

public record PostUpdateRequest(String tenantId, String code, String name, Long departmentId, String status) {
}
