package com.github.thundax.bacon.upms.interfaces.dto;

public record PostUpdateRequest(Long tenantId, String code, String name, Long departmentId, String status) {
}
