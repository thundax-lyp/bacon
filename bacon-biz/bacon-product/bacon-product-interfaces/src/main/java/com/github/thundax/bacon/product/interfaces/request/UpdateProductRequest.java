package com.github.thundax.bacon.product.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProductRequest(
        @NotBlank String spuName,
        @NotNull Long categoryId,
        String description,
        String mainImageObjectId,
        @NotNull Long expectedVersion,
        @NotBlank String idempotencyKey) {}
