package com.github.thundax.bacon.upms.infra.repository.impl;

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
    public Optional<Tenant> findById(TenantId tenantId) {
        return support.findById(tenantId);
    }

    @Override
    public Optional<Tenant> findByCode(TenantCode code) {
        return Optional.ofNullable(code).flatMap(item -> support.findByCode(item.value()));
    }

    @Override
    public List<Tenant> page(String name, TenantStatus status, int pageNo, int pageSize) {
        return support.listTenants(name, status == null ? null : status.value(), pageNo, pageSize);
    }

    @Override
    public long count(String name, TenantStatus status) {
        return support.count(name, status == null ? null : status.value());
    }

    @Override
    public Tenant insert(Tenant tenant) {
        return support.saveTenant(tenant);
    }

    @Override
    public Tenant update(Tenant tenant) {
        return support.saveTenant(tenant);
    }

}
