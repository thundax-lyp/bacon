package com.github.thundax.bacon.common.commerce.codec;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;

public final class SkuIdCodec {

    private SkuIdCodec() {}

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
