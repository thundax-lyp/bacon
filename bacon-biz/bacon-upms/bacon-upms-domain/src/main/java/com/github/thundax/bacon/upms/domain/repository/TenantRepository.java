package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.entity.Tenant;
import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findTenantByTenantId(Long tenantId);

    Optional<Tenant> findTenantByCode(String code);

    List<Tenant> pageTenants(Long tenantId, String code, String name, String status, int pageNo, int pageSize);

    long countTenants(Long tenantId, String code, String name, String status);

    Tenant saveTenant(Tenant tenant);

    Tenant updateTenantStatus(Long tenantId, String status);
}
