package com.github.thundax.bacon.upms.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RoleStatusUpdateRequest(
        @Schema(
                        description = "启用状态",
                        allowableValues = {"ENABLED", "DISABLED"},
                        example = "ENABLED")
                String status) {}
