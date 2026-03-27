package com.github.thundax.bacon.common.security.context;

import org.springframework.beans.factory.ObjectProvider;

public class SpringContextCurrentTenantProvider implements CurrentTenantProvider {

    private final ObjectProvider<CurrentTenantResolver> currentTenantResolver;

    public SpringContextCurrentTenantProvider(ObjectProvider<CurrentTenantResolver> currentTenantResolver) {
        this.currentTenantResolver = currentTenantResolver;
    }

    @Override
    public Long currentTenantId() {
        CurrentTenantResolver resolver = currentTenantResolver.getIfAvailable();
        return resolver == null ? null : resolver.currentTenantId();
    }
}
