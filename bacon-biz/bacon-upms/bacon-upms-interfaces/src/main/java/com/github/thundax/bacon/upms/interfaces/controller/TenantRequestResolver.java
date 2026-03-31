package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.id.domain.TenantId;
import org.springframework.stereotype.Component;

@Component
class TenantRequestResolver {

    TenantId resolveTenantId(String tenantId) {
        return TenantId.of(tenantId);
    }
}
