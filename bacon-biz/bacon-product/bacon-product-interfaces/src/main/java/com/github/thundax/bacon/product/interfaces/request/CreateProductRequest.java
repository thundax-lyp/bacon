package com.github.thundax.bacon.product.interfaces.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateProductRequest(
        String spuCode,
        @NotBlank String spuName,
        @NotNull Long categoryId,
        String description,
        String mainImageObjectId,
        @NotEmpty List<@Valid CreateProductSkuRequest> skus,
        @NotBlank String idempotencyKey) {}
