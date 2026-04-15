package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findTenantById(TenantId tenantId);

    Optional<Tenant> findTenantByCode(TenantCode tenantCode);

    List<Tenant> pageTenants(String name, TenantStatus status, int pageNo, int pageSize);

    long countTenants(String name, TenantStatus status);

    Tenant insert(Tenant tenant);

    Tenant save(Tenant tenant);

    Tenant updateStatus(TenantId tenantId, TenantStatus status);
}
