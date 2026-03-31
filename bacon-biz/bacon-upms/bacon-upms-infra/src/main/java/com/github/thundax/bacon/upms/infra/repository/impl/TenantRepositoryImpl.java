package com.github.thundax.bacon.upms.infra.repository.impl;

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
    public Optional<Tenant> findTenantByTenantId(Long tenantId) {
        return support.findTenantByTenantId(tenantId);
    }

    @Override
    public Optional<Tenant> findTenantByCode(String code) {
        return support.findTenantByCode(code);
    }

    @Override
    public List<Tenant> pageTenants(Long tenantId, String code, String name, String status, int pageNo, int pageSize) {
        return support.listTenants(tenantId, code, name, status, pageNo, pageSize);
    }

    @Override
    public long countTenants(Long tenantId, String code, String name, String status) {
        return support.countTenants(tenantId, code, name, status);
    }

    @Override
    public Tenant saveTenant(Tenant tenant) {
        return support.saveTenant(tenant.getId() == null
                ? new Tenant(null, null, tenant.getCode(), tenant.getName(), tenant.getStatus(),
                tenant.getCreatedBy(), tenant.getCreatedAt(), tenant.getUpdatedBy(), tenant.getUpdatedAt())
                : tenant);
    }

    @Override
    public Tenant updateTenantStatus(Long tenantId, String status) {
        Tenant currentTenant = findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return support.saveTenant(new Tenant(
                currentTenant.getId(),
                currentTenant.getTenantId(),
                currentTenant.getCode(),
                currentTenant.getName(),
                status,
                currentTenant.getCreatedBy(),
                currentTenant.getCreatedAt(),
                currentTenant.getUpdatedBy(),
                currentTenant.getUpdatedAt()));
    }
}
