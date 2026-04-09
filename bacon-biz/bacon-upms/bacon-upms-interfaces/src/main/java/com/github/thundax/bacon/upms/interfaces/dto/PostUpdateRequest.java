package com.github.thundax.bacon.upms.interfaces.dto;

public record PostUpdateRequest(String tenantCode, String code, String name, String departmentId, String status) {}
