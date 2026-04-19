package com.github.thundax.bacon.upms.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResourceCreateRequest(
        @NotBlank(message = "code must not be blank") @Size(max = 64, message = "code length must be <= 64")
                String code,
        @NotBlank(message = "name must not be blank") @Size(max = 128, message = "name length must be <= 128")
                String name,
        @Schema(
                        description = "资源类型",
                        allowableValues = {"API", "RPC", "EVENT"},
                        example = "API")
                String resourceType,
        @NotBlank(message = "httpMethod must not be blank")
                @Size(max = 16, message = "httpMethod length must be <= 16")
                String httpMethod,
        @NotBlank(message = "uri must not be blank") @Size(max = 255, message = "uri length must be <= 255")
                String uri) {}
