package com.github.thundax.bacon.upms.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

public record RoleDataScopeAssignRequest(
        @Schema(
                        description = "数据范围类型",
                        allowableValues = {"ALL", "DEPARTMENT", "DEPARTMENT_AND_CHILDREN", "SELF", "CUSTOM"},
                        example = "DEPARTMENT")
                String dataScopeType,
        Set<Long> departmentIds) {}
