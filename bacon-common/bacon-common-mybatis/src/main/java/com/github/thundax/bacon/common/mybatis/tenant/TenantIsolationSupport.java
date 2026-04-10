package com.github.thundax.bacon.common.mybatis.tenant;

import com.github.thundax.bacon.common.mybatis.annotation.TenantIsolated;

public final class TenantIsolationSupport {

    private TenantIsolationSupport() {}

    public static boolean isTenantIsolated(Class<?> entityType) {
        return entityType != null && entityType.isAnnotationPresent(TenantIsolated.class);
    }
}
