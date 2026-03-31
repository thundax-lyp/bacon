package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.upms.application.command.TenantApplicationService;
import org.springframework.stereotype.Component;

@Component
class TenantRequestResolver {

    private final TenantApplicationService tenantApplicationService;

    TenantRequestResolver(TenantApplicationService tenantApplicationService) {
        this.tenantApplicationService = tenantApplicationService;
    }

    Long resolveTenantId(String tenantNo) {
        return tenantApplicationService.getTenantByTenantNo(tenantNo).getId();
    }
}
