package com.github.thundax.bacon.upms.interfaces.dto;

public record UserCreateRequest(String tenantNo, String account, String name, String phone, Long departmentId) {
}
