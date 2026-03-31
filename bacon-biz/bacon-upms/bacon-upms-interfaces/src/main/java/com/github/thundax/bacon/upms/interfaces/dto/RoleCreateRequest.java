package com.github.thundax.bacon.upms.interfaces.dto;

public record RoleCreateRequest(String tenantId, String code, String name, String roleType, String dataScopeType) {
}
