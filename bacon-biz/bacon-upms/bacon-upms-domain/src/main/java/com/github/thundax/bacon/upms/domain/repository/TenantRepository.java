package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantNo;
import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findTenantById(Long tenantId);

    Optional<Tenant> findTenantByTenantNo(TenantNo tenantNo);

    List<Tenant> pageTenants(TenantNo tenantNo, String name, String status, int pageNo, int pageSize);

    long countTenants(TenantNo tenantNo, String name, String status);

    Tenant saveTenant(Tenant tenant);

    Tenant updateTenantStatus(TenantNo tenantNo, String status);
}
