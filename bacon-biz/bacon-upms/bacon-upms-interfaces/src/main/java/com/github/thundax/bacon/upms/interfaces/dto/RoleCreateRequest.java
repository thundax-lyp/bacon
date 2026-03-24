package com.github.thundax.bacon.upms.interfaces.dto;

public record RoleCreateRequest(Long tenantId, String code, String name, String roleType, String dataScopeType) {
}
