package com.github.thundax.bacon.upms.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RoleUpdateRequest(
        String code,
        String name,
        @Schema(
                        description = "角色类型",
                        allowableValues = {"SYSTEM_ROLE", "TENANT_ROLE", "CUSTOM_ROLE"},
                        example = "TENANT_ROLE")
                String roleType,
        @Schema(
                        description = "数据范围类型",
                        allowableValues = {"ALL", "DEPARTMENT", "DEPARTMENT_AND_CHILDREN", "SELF", "CUSTOM"},
                        example = "DEPARTMENT")
                String dataScopeType) {}
