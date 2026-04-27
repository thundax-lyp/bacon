package com.github.thundax.bacon.product.application.result;

import com.github.thundax.bacon.product.domain.model.enums.ProductStatus;

public record ProductCommandResult(Long tenantId, Long spuId, String spuCode, ProductStatus productStatus, Long productVersion) {}
