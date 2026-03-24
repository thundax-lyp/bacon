package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.TenantPageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.TenantPageResultDTO;
import com.github.thundax.bacon.upms.domain.entity.Tenant;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantApplicationService {

    private static final String DISABLED_STATUS = "DISABLED";

    private final UserRepository userRepository;
    private final SessionCommandFacade sessionCommandFacade;

    public TenantApplicationService(UserRepository userRepository, SessionCommandFacade sessionCommandFacade) {
        this.userRepository = userRepository;
        this.sessionCommandFacade = sessionCommandFacade;
    }

    public TenantPageResultDTO pageTenants(TenantPageQueryDTO query) {
        int pageNo = normalizePageNo(query.getPageNo());
        int pageSize = normalizePageSize(query.getPageSize());
        return new TenantPageResultDTO(userRepository.pageTenants(query.getTenantId(), query.getCode(), query.getName(),
                query.getStatus(), pageNo, pageSize).stream().map(this::toDto).toList(),
                userRepository.countTenants(query.getTenantId(), query.getCode(), query.getName(), query.getStatus()),
                pageNo, pageSize);
    }

    public TenantDTO createTenant(String code, String name) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        userRepository.findTenantByCode(normalize(code)).ifPresent(tenant -> {
            throw new IllegalArgumentException("Tenant code already exists: " + code);
        });
        return toDto(userRepository.saveTenant(new Tenant(null, null, normalize(code), normalize(name), "ENABLED")));
    }

    public TenantDTO updateTenant(Long tenantId, String code, String name) {
        Tenant currentTenant = requireTenant(tenantId);
        validateRequired(code, "code");
        validateRequired(name, "name");
        userRepository.findTenantByCode(normalize(code))
                .filter(tenant -> !tenant.getTenantId().equals(tenantId))
                .ifPresent(tenant -> {
                    throw new IllegalArgumentException("Tenant code already exists: " + code);
                });
        return toDto(userRepository.saveTenant(new Tenant(currentTenant.getId(), currentTenant.getCreatedBy(),
                currentTenant.getCreatedAt(), currentTenant.getUpdatedBy(), currentTenant.getUpdatedAt(), tenantId,
                normalize(code), normalize(name), currentTenant.getStatus())));
    }

    public TenantDTO updateTenantStatus(Long tenantId, String status) {
        validateRequired(status, "status");
        Tenant tenant = userRepository.updateTenantStatus(tenantId, normalize(status));
        if (DISABLED_STATUS.equalsIgnoreCase(tenant.getStatus())) {
            sessionCommandFacade.invalidateTenantSessions(tenantId, "TENANT_DISABLED");
        }
        return toDto(tenant);
    }

    private Tenant requireTenant(Long tenantId) {
        return userRepository.findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    private TenantDTO toDto(Tenant tenant) {
        return new TenantDTO(tenant.getId(), tenant.getTenantId(), tenant.getCode(), tenant.getName(), tenant.getStatus());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 20 : pageSize;
    }
}
