package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantPersistenceSupport support;

    public TenantRepositoryImpl(TenantPersistenceSupport support) {
        this.support = support;
    }

    @Override
    public Optional<Tenant> findTenantById(TenantId tenantId) {
        return support.findTenantById(tenantId);
    }

    @Override
    public Optional<Tenant> findTenantByTenantId(TenantId tenantId) {
        return support.findTenantByTenantId(tenantId);
    }

    @Override
    public Optional<Tenant> findTenantByCode(String tenantCode) {
        return support.findTenantByCode(tenantCode);
    }

    @Override
    public List<Tenant> pageTenants(String name, String status, int pageNo, int pageSize) {
        return support.listTenants(name, status, pageNo, pageSize);
    }

    @Override
    public long countTenants(String name, String status) {
        return support.countTenants(name, status);
    }

    @Override
    public Tenant saveTenant(Tenant tenant) {
        return support.saveTenant(tenant);
    }

    @Override
    public Tenant updateTenantStatus(TenantId tenantId, String status) {
        Tenant currentTenant = findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId.value()));
        return support.saveTenant(Tenant.create(
                currentTenant.getId(),
                currentTenant.getName(),
                currentTenant.getTenantCode(),
                TenantStatus.from(status),
                currentTenant.getExpiredAt()));
    }
}
