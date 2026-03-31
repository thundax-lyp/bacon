package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(TenantPersistenceSupport.class)
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
    public List<Tenant> pageTenants(TenantId tenantId, String name, String status, int pageNo, int pageSize) {
        return support.listTenants(tenantId, name, status, pageNo, pageSize);
    }

    @Override
    public long countTenants(TenantId tenantId, String name, String status) {
        return support.countTenants(tenantId, name, status);
    }

    @Override
    public Tenant saveTenant(Tenant tenant) {
        return support.saveTenant(tenant);
    }

    @Override
    public Tenant updateTenantStatus(TenantId tenantId, String status) {
        Tenant currentTenant = findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId.value()));
        return support.saveTenant(new Tenant(
                currentTenant.getId(),
                currentTenant.getName(),
                com.github.thundax.bacon.upms.domain.model.enums.TenantStatus.fromValue(status),
                currentTenant.getCreatedBy(),
                currentTenant.getCreatedAt(),
                currentTenant.getUpdatedBy(),
                currentTenant.getUpdatedAt()));
    }
}
