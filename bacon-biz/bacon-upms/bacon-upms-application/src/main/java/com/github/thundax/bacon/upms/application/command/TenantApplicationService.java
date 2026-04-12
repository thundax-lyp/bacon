package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.TenantPageQueryDTO;
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

    public PageResultDTO<TenantDTO> pageTenants(TenantPageQueryDTO query) {
        // 租户分页属于运营后台能力，统一先归一化分页参数，避免不同入口传入 0/负数时结果漂移。
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        TenantId tenantId = toTenantId(query.getTenantId());
        return new PageResultDTO<>(
                tenantRepository.pageTenants(tenantId, query.getName(), query.getStatus(), pageNo, pageSize).stream()
                        .map(this::toDto)
                        .toList(),
                tenantRepository.countTenants(tenantId, query.getName(), query.getStatus()),
                pageNo,
                pageSize);
    }

    @Transactional
    public TenantDTO createTenant(Long tenantId, String name, String tenantCode, Instant expiredAt) {
        validateRequired(name, "name");
        validateRequired(tenantCode, "tenantCode");
        TenantId normalizedTenantId = TenantId.of(tenantId);
        TenantCode normalizedTenantCode = TenantCode.of(tenantCode);
        tenantRepository.findTenantByTenantId(normalizedTenantId).ifPresent(tenant -> {
            throw new IllegalArgumentException("Tenant tenantId already exists: " + normalizedTenantId.value());
        });
        tenantRepository.findTenantByCode(normalizedTenantCode.value()).ifPresent(tenant -> {
            throw new IllegalArgumentException("Tenant tenantCode already exists: " + normalizedTenantCode.value());
        });
        return toDto(tenantRepository.saveTenant(Tenant.create(
                normalizedTenantId,
                normalize(name),
                normalizedTenantCode,
                TenantStatus.ACTIVE,
                expiredAt,
                null,
                null,
                null,
                null)));
    }

    @Transactional
    public TenantDTO updateTenant(Long tenantId, String name, String tenantCode, Instant expiredAt) {
        validateRequired(name, "name");
        validateRequired(tenantCode, "tenantCode");
        TenantId normalizedTenantId = TenantId.of(tenantId);
        Tenant currentTenant = requireTenant(normalizedTenantId);
        TenantCode normalizedTenantCode = TenantCode.of(tenantCode);
        tenantRepository
                .findTenantByCode(normalizedTenantCode.value())
                .filter(tenant -> !tenant.getId().equals(normalizedTenantId))
                .ifPresent(tenant -> {
                    throw new IllegalArgumentException(
                            "Tenant tenantCode already exists: " + normalizedTenantCode.value());
                });
        return toDto(tenantRepository.saveTenant(Tenant.reconstruct(
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

    @Transactional
    public TenantDTO updateTenantStatus(Long tenantId, TenantStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        TenantId normalizedTenantId = TenantId.of(tenantId);
        Tenant tenant = tenantRepository.updateTenantStatus(normalizedTenantId, status.value());
        // 租户停用要同步踢出该租户下所有会话，否则鉴权缓存里仍会保留已禁用租户的访问上下文。
        if (TenantStatus.DISABLED == tenant.getStatus()) {
            sessionCommandFacade.invalidateTenantSessions(tenant.getId().value(), "TENANT_DISABLED");
        }
        return toDto(tenant);
    }

    public TenantDTO getTenantByTenantId(Long tenantId) {
        return toDto(requireTenant(TenantId.of(tenantId)));
    }

    private Tenant requireTenant(TenantId tenantId) {
        return tenantRepository
                .findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId.value()));
    }

    private TenantDTO toDto(Tenant tenant) {
        return new TenantDTO(
                tenant.getId(),
                tenant.getName(),
                tenant.getTenantCode().value(),
                tenant.getStatus().value(),
                tenant.getExpiredAt());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private TenantId toTenantId(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        return TenantId.of(tenantId);
    }
}
