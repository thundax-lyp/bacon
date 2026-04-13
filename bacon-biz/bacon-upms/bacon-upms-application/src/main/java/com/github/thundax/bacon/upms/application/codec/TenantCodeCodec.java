package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;

public final class TenantCodeCodec {

    private TenantCodeCodec() {}

    public static TenantCode toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TenantCode.of(value.trim());
    }

    public static String toValue(TenantCode value) {
        return value == null ? null : value.value();
    }
}
