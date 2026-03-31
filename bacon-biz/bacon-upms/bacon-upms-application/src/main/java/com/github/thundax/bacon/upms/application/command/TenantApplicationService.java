package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.TenantPageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.TenantPageResultDTO;
import com.github.thundax.bacon.upms.api.enums.UpmsStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantApplicationService {

    private final TenantRepository tenantRepository;
    private final SessionCommandFacade sessionCommandFacade;

    public TenantApplicationService(TenantRepository tenantRepository, SessionCommandFacade sessionCommandFacade) {
        this.tenantRepository = tenantRepository;
        this.sessionCommandFacade = sessionCommandFacade;
    }

    public TenantPageResultDTO pageTenants(TenantPageQueryDTO query) {
        // 租户分页属于运营后台能力，统一先归一化分页参数，避免不同入口传入 0/负数时结果漂移。
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        return new TenantPageResultDTO(tenantRepository.pageTenants(query.getTenantNo(), query.getName(),
                query.getStatus(), pageNo, pageSize).stream().map(this::toDto).toList(),
                tenantRepository.countTenants(query.getTenantNo(), query.getName(), query.getStatus()),
                pageNo, pageSize);
    }

    public TenantDTO createTenant(String tenantNo, String name) {
        validateRequired(tenantNo, "tenantNo");
        validateRequired(name, "name");
        tenantRepository.findTenantByTenantNo(normalize(tenantNo)).ifPresent(tenant -> {
            throw new IllegalArgumentException("Tenant tenantNo already exists: " + tenantNo);
        });
        return toDto(tenantRepository.saveTenant(new Tenant(null, normalize(tenantNo), normalize(name),
                TenantStatus.ENABLED)));
    }

    public TenantDTO updateTenant(String tenantNo, String name) {
        Tenant currentTenant = requireTenant(tenantNo);
        validateRequired(name, "name");
        return toDto(tenantRepository.saveTenant(new Tenant(
                currentTenant.getId(),
                currentTenant.getTenantNo(),
                normalize(name),
                currentTenant.getStatus(),
                currentTenant.getCreatedBy(),
                currentTenant.getCreatedAt(),
                currentTenant.getUpdatedBy(),
                currentTenant.getUpdatedAt())));
    }

    public TenantDTO updateTenantStatus(String tenantNo, UpmsStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        Tenant tenant = tenantRepository.updateTenantStatus(tenantNo, status.value());
        // 租户停用要同步踢出该租户下所有会话，否则鉴权缓存里仍会保留已禁用租户的访问上下文。
        if (TenantStatus.DISABLED == tenant.getStatus()) {
            sessionCommandFacade.invalidateTenantSessions(tenant.getTenantNo(), "TENANT_DISABLED");
        }
        return toDto(tenant);
    }

    public TenantDTO getTenantByTenantNo(String tenantNo) {
        return toDto(requireTenant(tenantNo));
    }

    private Tenant requireTenant(String tenantNo) {
        return tenantRepository.findTenantByTenantNo(tenantNo)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantNo));
    }

    private TenantDTO toDto(Tenant tenant) {
        return new TenantDTO(tenant.getId(), tenant.getTenantNo(), tenant.getName(), tenant.getStatus().value());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

}
