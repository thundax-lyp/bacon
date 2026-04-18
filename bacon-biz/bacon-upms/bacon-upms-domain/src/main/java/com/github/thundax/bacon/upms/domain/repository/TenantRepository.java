package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findById(TenantId tenantId);

    Optional<Tenant> findByCode(TenantCode tenantCode);

    List<Tenant> page(String name, TenantStatus status, int pageNo, int pageSize);

    long count(String name, TenantStatus status);

    Tenant insert(Tenant tenant);

    Tenant update(Tenant tenant);

}
