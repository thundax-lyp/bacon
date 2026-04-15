package com.github.thundax.bacon.upms.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TenantStatusUpdateRequest(
        @Schema(
                        description = "租户状态",
                        allowableValues = {"ENABLED", "DISABLED"},
                        example = "ENABLED")
                String status) {}
