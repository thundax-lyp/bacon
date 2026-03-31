package com.github.thundax.bacon.upms.interfaces.dto;

public record RoleCreateRequest(String tenantNo, String code, String name, String roleType, String dataScopeType) {
}
