package com.github.thundax.bacon.common.security.context;

public class DefaultCurrentTenantProvider implements CurrentTenantProvider {

    @Override
    public Long currentTenantId() {
        return null;
    }
}
