package com.github.thundax.bacon.upms.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePageRequest {

    private String code;
    private String name;

    @Schema(
            description = "资源类型",
            allowableValues = {"API", "RPC", "EVENT"},
            example = "API")
    private String resourceType;

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
