package com.github.thundax.bacon.product.interfaces.response;

public record ProductCommandResponse(
        Long tenantId, Long spuId, String spuCode, String productStatus, Long productVersion) {}
