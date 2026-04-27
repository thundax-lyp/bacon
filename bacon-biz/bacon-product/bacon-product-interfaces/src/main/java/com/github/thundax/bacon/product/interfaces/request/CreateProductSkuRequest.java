package com.github.thundax.bacon.product.interfaces.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateProductSkuRequest(
        @NotBlank String skuCode,
        @NotBlank String skuName,
        @NotBlank String specAttributes,
        @NotNull @DecimalMin("0.00") BigDecimal salePrice) {}
