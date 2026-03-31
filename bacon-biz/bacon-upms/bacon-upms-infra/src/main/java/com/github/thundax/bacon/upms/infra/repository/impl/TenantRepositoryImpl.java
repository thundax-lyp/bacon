package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantNo;
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
    public Optional<Tenant> findTenantById(Long tenantId) {
        return support.findTenantById(tenantId);
    }

    @Override
    public Optional<Tenant> findTenantByTenantNo(TenantNo tenantNo) {
        return support.findTenantByTenantNo(tenantNo);
    }

    @Override
    public List<Tenant> pageTenants(TenantNo tenantNo, String name, String status, int pageNo, int pageSize) {
        return support.listTenants(tenantNo, name, status, pageNo, pageSize);
    }

    @Override
    public long countTenants(TenantNo tenantNo, String name, String status) {
        return support.countTenants(tenantNo, name, status);
    }

    @Override
    public Tenant saveTenant(Tenant tenant) {
        return support.saveTenant(tenant.getId() == null
                ? new Tenant(null, tenant.getTenantNo(), tenant.getName(), tenant.getStatus(),
                tenant.getCreatedBy(), tenant.getCreatedAt(), tenant.getUpdatedBy(), tenant.getUpdatedAt())
                : tenant);
    }

    @Override
    public Tenant updateTenantStatus(TenantNo tenantNo, String status) {
        Tenant currentTenant = findTenantByTenantNo(tenantNo)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantNo.value()));
        return support.saveTenant(new Tenant(
                currentTenant.getId(),
                currentTenant.getTenantNo(),
                currentTenant.getName(),
                com.github.thundax.bacon.upms.domain.model.enums.TenantStatus.fromValue(status),
                currentTenant.getCreatedBy(),
                currentTenant.getCreatedAt(),
                currentTenant.getUpdatedBy(),
                currentTenant.getUpdatedAt()));
    }
}
