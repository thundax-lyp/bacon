package com.github.thundax.bacon.upms.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RoleStatusUpdateRequest(
        @NotBlank(message = "status must not be blank")
        @Schema(
                        description = "启用状态",
                        allowableValues = {"ENABLED", "DISABLED"},
                        example = "ENABLED")
                String status) {}
