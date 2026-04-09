package com.github.thundax.bacon.upms.interfaces.dto;

public record PostCreateRequest(String tenantCode, String code, String name, String departmentId) {}
