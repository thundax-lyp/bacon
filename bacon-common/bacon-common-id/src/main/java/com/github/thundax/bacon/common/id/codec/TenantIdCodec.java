package com.github.thundax.bacon.common.id.codec;

import com.github.thundax.bacon.common.id.domain.TenantId;

public final class TenantIdCodec {

    private TenantIdCodec() {}

    public static TenantId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return TenantId.of(value);
    }

    public static Long toValue(TenantId tenantId) {
        return tenantId == null ? null : tenantId.value();
    }
}
