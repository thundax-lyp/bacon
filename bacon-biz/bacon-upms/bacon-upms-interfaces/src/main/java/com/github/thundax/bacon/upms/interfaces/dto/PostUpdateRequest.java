package com.github.thundax.bacon.upms.interfaces.dto;

public record PostUpdateRequest(String tenantNo, String code, String name, Long departmentId, String status) {
}
