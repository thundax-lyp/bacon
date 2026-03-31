package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findTenantByTenantNo(String tenantNo);

    List<Tenant> pageTenants(String tenantNo, String name, String status, int pageNo, int pageSize);

    long countTenants(String tenantNo, String name, String status);

    Tenant saveTenant(Tenant tenant);

    Tenant updateTenantStatus(String tenantNo, String status);
}
