package com.github.thundax.bacon.upms.interfaces.dto;

public record PostCreateRequest(String tenantNo, String code, String name, Long departmentId) {
}
