package com.github.thundax.bacon.upms.interfaces.dto;

public record UserUpdateRequest(Long tenantId, String account, String name, String phone, Long departmentId) {
}
