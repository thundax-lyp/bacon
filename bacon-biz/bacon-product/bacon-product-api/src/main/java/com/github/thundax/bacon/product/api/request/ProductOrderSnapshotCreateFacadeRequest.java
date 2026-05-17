package com.github.thundax.bacon.product.api.request;

public record ProductOrderSnapshotCreateFacadeRequest(
        Long tenantId, String orderNo, String orderItemNo, Long skuId, Integer quantity) {}
