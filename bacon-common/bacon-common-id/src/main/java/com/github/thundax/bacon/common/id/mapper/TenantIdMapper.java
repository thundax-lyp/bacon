package com.github.thundax.bacon.common.id.mapper;

import com.github.thundax.bacon.common.id.domain.TenantId;

public final class TenantIdMapper {

    private TenantIdMapper() {}

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
