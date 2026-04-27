package com.github.thundax.bacon.product.application.command;

public record UpdateProductCommand(
        Long tenantId,
        Long spuId,
        String spuName,
        Long categoryId,
        String description,
        String mainImageObjectId,
        Long expectedVersion,
        String idempotencyKey) {}
