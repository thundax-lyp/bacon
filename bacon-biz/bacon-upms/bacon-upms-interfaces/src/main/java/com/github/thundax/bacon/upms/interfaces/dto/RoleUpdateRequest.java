package com.github.thundax.bacon.upms.interfaces.dto;

public record RoleUpdateRequest(String tenantCode, String code, String name, String roleType, String dataScopeType) {
}
