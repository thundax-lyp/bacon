package com.github.thundax.bacon.upms.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResourceCreateRequest(
        String code,
        String name,
        @Schema(
                        description = "资源类型",
                        allowableValues = {"API", "RPC", "EVENT"},
                        example = "API")
                String resourceType,
        String httpMethod,
        String uri) {}
