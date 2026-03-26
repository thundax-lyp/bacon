package com.github.thundax.bacon.common.security.context;

public class MonoCurrentTenantProvider implements CurrentTenantProvider {

    private final CurrentTenantResolver currentTenantResolver;

    public MonoCurrentTenantProvider(CurrentTenantResolver currentTenantResolver) {
        this.currentTenantResolver = currentTenantResolver;
    }

    @Override
    public Long currentTenantId() {
        return currentTenantResolver.currentTenantId();
    }
}
