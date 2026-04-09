package com.github.thundax.bacon.common.commerce.mapper;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;

public final class SkuIdMapper {

    private SkuIdMapper() {}

    public static SkuId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return SkuId.of(value);
    }

    public static Long toValue(SkuId skuId) {
        return skuId == null ? null : skuId.value();
    }
}
