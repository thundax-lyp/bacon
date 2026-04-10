package com.github.thundax.bacon.common.mybatis.tenant;

import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;

public final class TenantScopeSupport {

    private TenantScopeSupport() {}

    public static TenantScoped getTenantScoped(Class<?> entityType) {
        return entityType == null ? null : entityType.getAnnotation(TenantScoped.class);
    }

    public static boolean isReadEnabled(Class<?> entityType) {
        TenantScoped tenantScoped = getTenantScoped(entityType);
        return tenantScoped != null && tenantScoped.read();
    }

    public static boolean isInsertEnabled(Class<?> entityType) {
        TenantScoped tenantScoped = getTenantScoped(entityType);
        return tenantScoped != null && tenantScoped.insert();
    }

    public static boolean isVerifyOnUpdateEnabled(Class<?> entityType) {
        TenantScoped tenantScoped = getTenantScoped(entityType);
        return tenantScoped != null && tenantScoped.verifyOnUpdate();
    }
}
