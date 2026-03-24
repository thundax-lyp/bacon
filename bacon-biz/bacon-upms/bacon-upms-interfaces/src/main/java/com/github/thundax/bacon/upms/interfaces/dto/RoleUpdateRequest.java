package com.github.thundax.bacon.upms.interfaces.dto;

public record RoleUpdateRequest(Long tenantId, String code, String name, String roleType, String dataScopeType) {
}
