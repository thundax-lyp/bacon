package com.github.thundax.bacon.product.application.command;

public record CreateOrderProductSnapshotCommand(
        Long tenantId, String orderNo, String orderItemNo, Long skuId, Integer quantity) {}
