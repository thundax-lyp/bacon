package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TenantRepositoryImpl implements TenantRepository {

    private final InMemoryUpmsStore upmsStore;

    public TenantRepositoryImpl(InMemoryUpmsStore upmsStore) {
        this.upmsStore = upmsStore;
    }

    @Override
    public Optional<Tenant> findTenantByTenantId(Long tenantId) {
        return Optional.ofNullable(upmsStore.getTenants().get(tenantId));
    }

    @Override
    public Optional<Tenant> findTenantByCode(String code) {
        return upmsStore.getTenants().values().stream()
                .filter(tenant -> tenant.getCode().equals(code))
                .findFirst();
    }

    @Override
    public List<Tenant> pageTenants(Long tenantId, String code, String name, String status, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return filteredTenants(tenantId, code, name, status).stream()
                .skip(offset)
                .limit(pageSize)
                .toList();
    }

    @Override
    public long countTenants(Long tenantId, String code, String name, String status) {
        return filteredTenants(tenantId, code, name, status).size();
    }

    @Override
    public Tenant saveTenant(Tenant tenant) {
        Tenant savedTenant;
        if (tenant.getId() == null) {
            Long generatedTenantId = upmsStore.nextTenantId();
            savedTenant = new Tenant(generatedTenantId, generatedTenantId, tenant.getCode(), tenant.getName(), tenant.getStatus());
        } else {
            savedTenant = tenant;
        }
        upmsStore.getTenants().put(savedTenant.getTenantId(), savedTenant);
        return savedTenant;
    }

    @Override
    public Tenant updateTenantStatus(Long tenantId, String status) {
        Tenant currentTenant = findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        Tenant updatedTenant = new Tenant(currentTenant.getId(), currentTenant.getCreatedBy(), currentTenant.getCreatedAt(),
                currentTenant.getUpdatedBy(), currentTenant.getUpdatedAt(), currentTenant.getTenantId(), currentTenant.getCode(),
                currentTenant.getName(), status);
        upmsStore.getTenants().put(tenantId, updatedTenant);
        return updatedTenant;
    }

    private List<Tenant> filteredTenants(Long tenantId, String code, String name, String status) {
        return upmsStore.getTenants().values().stream()
                .filter(tenant -> tenantId == null || tenantId.equals(tenant.getTenantId()))
                .filter(tenant -> matchContains(tenant.getCode(), code))
                .filter(tenant -> matchContains(tenant.getName(), name))
                .filter(tenant -> matchEquals(tenant.getStatus(), status))
                .sorted(Comparator.comparing(Tenant::getTenantId))
                .toList();
    }

    private boolean matchContains(String actual, String expected) {
        return expected == null || expected.isBlank() || (actual != null && actual.contains(expected.trim()));
    }

    private boolean matchEquals(String actual, String expected) {
        return expected == null || expected.isBlank() || (actual != null && actual.equalsIgnoreCase(expected.trim()));
    }
}
