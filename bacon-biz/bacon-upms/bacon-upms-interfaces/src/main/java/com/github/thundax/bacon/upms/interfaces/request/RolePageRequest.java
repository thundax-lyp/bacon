package com.github.thundax.bacon.upms.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePageRequest {

    private String code;
    private String name;

    @Schema(
            description = "角色类型",
            allowableValues = {"SYSTEM_ROLE", "TENANT_ROLE", "CUSTOM_ROLE"},
            example = "TENANT_ROLE")
    private String roleType;

    @Schema(
            description = "启用状态",
            allowableValues = {"ENABLED", "DISABLED"},
            example = "ENABLED")
    private String status;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(200)
    private Integer pageSize;
}
