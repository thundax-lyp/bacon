package com.github.thundax.bacon.upms.interfaces.resolver;

import com.github.thundax.bacon.common.id.domain.TenantId;
import org.springframework.stereotype.Component;

@Component
public class TenantRequestResolver {

    public TenantId resolveTenantId(String tenantId) {
        return TenantId.of(tenantId);
    }
}
