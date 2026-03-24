package com.github.thundax.bacon.upms.interfaces.dto;

public record PostCreateRequest(Long tenantId, String code, String name, Long departmentId) {
}
