package com.github.thundax.bacon.upms.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantPageRequest {

    private String name;

    @Schema(
            description = "租户状态",
            allowableValues = {"ENABLED", "DISABLED"},
            example = "ENABLED")
    private String status;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(200)
    private Integer pageSize;
}
