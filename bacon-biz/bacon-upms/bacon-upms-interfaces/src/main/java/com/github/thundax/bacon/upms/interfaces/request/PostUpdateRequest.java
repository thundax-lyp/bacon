package com.github.thundax.bacon.upms.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostUpdateRequest(
        String code,
        String name,
        Long departmentId,
        @Schema(
                        description = "启用状态",
                        allowableValues = {"ENABLED", "DISABLED"},
                        example = "ENABLED")
                String status) {}
