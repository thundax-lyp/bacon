package com.github.thundax.bacon.product.application.command;

public record ChangeProductStatusCommand(Long tenantId, Long spuId, Long expectedVersion, String idempotencyKey) {}
