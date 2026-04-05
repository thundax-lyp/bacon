package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.TenantPageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.TenantPageResultDTO;
import com.github.thundax.bacon.upms.api.enums.TenantStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        TenantId tenantId = toTenantId(query.getTenantId());
        return new TenantPageResultDTO(tenantRepository.pageTenants(tenantId, query.getName(),
                query.getStatus(), pageNo, pageSize).stream().map(this::toDto).toList(),
                tenantRepository.countTenants(tenantId, query.getName(), query.getStatus()),
                pageNo, pageSize);
    }

    public TenantDTO createTenant(String tenantId, String name, String tenantCode, Instant expiredAt) {
        return createTenant(TenantId.of(tenantId), name, tenantCode, expiredAt);
    }

    @Transactional
    public TenantDTO createTenant(TenantId tenantId, String name, String tenantCode, Instant expiredAt) {
        validateRequired(name, "name");
        validateRequired(tenantCode, "tenantCode");
        TenantCode normalizedTenantCode = TenantCode.of(tenantCode);
        tenantRepository.findTenantByTenantId(tenantId).ifPresent(tenant -> {
            throw new IllegalArgumentException("Tenant tenantId already exists: " + tenantId.value());
        });
        tenantRepository.findTenantByCode(normalizedTenantCode.value()).ifPresent(tenant -> {
            throw new IllegalArgumentException("Tenant tenantCode already exists: " + normalizedTenantCode.value());
        });
        return toDto(tenantRepository.saveTenant(new Tenant(tenantId, normalize(name),
                normalizedTenantCode, TenantStatus.ACTIVE, expiredAt)));
    }

    public TenantDTO updateTenant(String tenantId, String name, String tenantCode, Instant expiredAt) {
        return updateTenant(TenantId.of(tenantId), name, tenantCode, expiredAt);
    }

    @Transactional
    public TenantDTO updateTenant(TenantId tenantId, String name, String tenantCode, Instant expiredAt) {
        Tenant currentTenant = requireTenant(tenantId);
        validateRequired(name, "name");
        validateRequired(tenantCode, "tenantCode");
        TenantCode normalizedTenantCode = TenantCode.of(tenantCode);
        tenantRepository.findTenantByCode(normalizedTenantCode.value())
                .filter(tenant -> !tenant.getId().equals(tenantId))
                .ifPresent(tenant -> {
                    throw new IllegalArgumentException("Tenant tenantCode already exists: " + normalizedTenantCode.value());
                });
        return toDto(tenantRepository.saveTenant(new Tenant(
                currentTenant.getId(),
                normalize(name),
                normalizedTenantCode,
                currentTenant.getStatus(),
                expiredAt,
                currentTenant.getCreatedBy(),
                currentTenant.getCreatedAt(),
                currentTenant.getUpdatedBy(),
                currentTenant.getUpdatedAt())));
    }

    public TenantDTO updateTenantStatus(String tenantId, TenantStatusEnum status) {
        return updateTenantStatus(TenantId.of(tenantId), status);
    }

    @Transactional
    public TenantDTO updateTenantStatus(TenantId tenantId, TenantStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        Tenant tenant = tenantRepository.updateTenantStatus(tenantId, status.value());
        // 租户停用要同步踢出该租户下所有会话，否则鉴权缓存里仍会保留已禁用租户的访问上下文。
        if (TenantStatus.DISABLED == tenant.getStatus()) {
            sessionCommandFacade.invalidateTenantSessions(String.valueOf(tenant.getId().value()), "TENANT_DISABLED");
        }
        return toDto(tenant);
    }

    public TenantDTO getTenantByTenantId(String tenantId) {
        return getTenantByTenantId(TenantId.of(tenantId));
    }

    public TenantDTO getTenantByTenantId(TenantId tenantId) {
        return toDto(requireTenant(tenantId));
    }

    private Tenant requireTenant(TenantId tenantId) {
        return tenantRepository.findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId.value()));
    }

    private TenantDTO toDto(Tenant tenant) {
        return new TenantDTO(tenant.getId(), tenant.getName(), tenant.getTenantCode().value(),
                tenant.getStatus().value(), tenant.getExpiredAt());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private TenantId toTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return null;
        }
        return TenantId.of(tenantId.trim());
    }

}
