package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
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
    public Optional<Tenant> findTenantByCode(TenantCode tenantCode) {
        return Optional.ofNullable(tenantCode).flatMap(code -> support.findTenantByCode(code.value()));
    }

    @Override
    public List<Tenant> pageTenants(String name, TenantStatus status, int pageNo, int pageSize) {
        return support.listTenants(name, status == null ? null : status.value(), pageNo, pageSize);
    }

    @Override
    public long countTenants(String name, TenantStatus status) {
        return support.countTenants(name, status == null ? null : status.value());
    }

    @Override
    public Tenant insert(Tenant tenant) {
        return support.saveTenant(tenant);
    }

    @Override
    public Tenant save(Tenant tenant) {
        return support.saveTenant(tenant);
    }

    @Override
    public Tenant updateStatus(TenantId tenantId, TenantStatus status) {
        Tenant currentTenant = findTenantById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantId.value()));
        return support.saveTenant(currentTenant.update(
                currentTenant.getName(), currentTenant.getTenantCode(), status, currentTenant.getExpiredAt()));
    }
}
