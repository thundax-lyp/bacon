package com.github.thundax.bacon.upms.interfaces.dto;

public record UserUpdateRequest(String tenantCode, String account, String name, String phone, Long departmentId) {}
