package com.github.thundax.bacon.upms.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Set;

public record RoleDataScopeAssignRequest(
        @NotBlank(message = "dataScopeType must not be blank")
        @Schema(
                        description = "数据范围类型",
                        allowableValues = {"ALL", "DEPARTMENT", "DEPARTMENT_AND_CHILDREN", "SELF", "CUSTOM"},
                        example = "DEPARTMENT")
                String dataScopeType,
        Set<@Positive(message = "departmentIds item must be greater than 0") Long> departmentIds) {}
