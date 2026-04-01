package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findTenantById(TenantId tenantId);

    Optional<Tenant> findTenantByTenantId(TenantId tenantId);

    Optional<Tenant> findTenantByCode(String tenantCode);

    List<Tenant> pageTenants(TenantId tenantId, String name, String status, int pageNo, int pageSize);

    long countTenants(TenantId tenantId, String name, String status);

    Tenant saveTenant(Tenant tenant);

    Tenant updateTenantStatus(TenantId tenantId, String status);
}
