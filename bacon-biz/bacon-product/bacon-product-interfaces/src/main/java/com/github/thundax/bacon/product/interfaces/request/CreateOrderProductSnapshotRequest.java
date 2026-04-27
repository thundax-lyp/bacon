package com.github.thundax.bacon.product.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderProductSnapshotRequest(
        @NotBlank String orderNo,
        @NotBlank String orderItemNo,
        @NotNull Long skuId,
        @NotNull @Positive Integer quantity) {}
