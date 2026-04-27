package com.github.thundax.bacon.product.application.command;

import java.util.List;

public record CreateProductCommand(
        Long tenantId,
        String spuCode,
        String spuName,
        Long categoryId,
        String description,
        String mainImageObjectId,
        List<CreateProductSkuCommand> skus,
        String idempotencyKey) {}
