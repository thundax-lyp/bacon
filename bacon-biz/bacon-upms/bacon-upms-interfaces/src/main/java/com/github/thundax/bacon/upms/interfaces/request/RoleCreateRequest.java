package com.github.thundax.bacon.upms.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest(
        @NotBlank(message = "code must not be blank") @Size(max = 64, message = "code length must be <= 64")
                String code,
        @NotBlank(message = "name must not be blank") @Size(max = 128, message = "name length must be <= 128")
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
