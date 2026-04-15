package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.application.assembler.TenantAssembler;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantApplicationService {

    private static final String TENANT_ID_BIZ_TAG = "tenant-id";

    private final TenantRepository tenantRepository;
    private final SessionCommandFacade sessionCommandFacade;
    private final IdGenerator idGenerator;

    public TenantApplicationService(
            TenantRepository tenantRepository, SessionCommandFacade sessionCommandFacade, IdGenerator idGenerator) {
        this.tenantRepository = tenantRepository;
        this.sessionCommandFacade = sessionCommandFacade;
        this.idGenerator = idGenerator;
    }

    public PageResultDTO<TenantDTO> pageTenants(String name, TenantStatus status, Integer pageNo, Integer pageSize) {
        // 租户分页属于运营后台能力，统一先归一化分页参数，避免不同入口传入 0/负数时结果漂移。
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        return new PageResultDTO<>(
                tenantRepository.pageTenants(name, status, normalizedPageNo, normalizedPageSize).stream()
                        .map(TenantAssembler::toDto)
                        .toList(),
                tenantRepository.countTenants(name, status),
                normalizedPageNo,
                normalizedPageSize);
    }

    @Transactional
    public TenantDTO createTenant(String name, TenantCode tenantCode, Instant expiredAt) {
        validateRequired(name, "name");
        if (tenantCode == null) {
            throw new IllegalArgumentException("tenantCode must not be null");
        }
        TenantId tenantId = TenantId.of(idGenerator.nextId(TENANT_ID_BIZ_TAG));
        tenantRepository.findTenantByCode(tenantCode).ifPresent(tenant -> {
            throw new IllegalArgumentException("Tenant tenantCode already exists: " + tenantCode.value());
        });
        return TenantAssembler.toDto(tenantRepository.insert(
                Tenant.create(tenantId, name.trim(), tenantCode, TenantStatus.ACTIVE, expiredAt)));
    }

    @Transactional
    public TenantDTO updateTenant(TenantId tenantId, String name, TenantCode tenantCode, Instant expiredAt) {
        validateRequired(name, "name");
        if (tenantCode == null) {
            throw new IllegalArgumentException("tenantCode must not be null");
        }
        Tenant currentTenant = requireTenant(tenantId);
        tenantRepository
                .findTenantByCode(tenantCode)
                .filter(tenant -> !tenant.getId().equals(tenantId))
                .ifPresent(tenant -> {
                    throw new IllegalArgumentException("Tenant tenantCode already exists: " + tenantCode.value());
                });
        return TenantAssembler.toDto(tenantRepository.save(
                currentTenant.update(name.trim(), tenantCode, currentTenant.getStatus(), expiredAt)));
    }

    @Transactional
    public TenantDTO updateTenantStatus(TenantId tenantId, TenantStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        Tenant tenant = tenantRepository.updateStatus(tenantId, status);
        // 租户停用要同步踢出该租户下所有会话，否则鉴权缓存里仍会保留已禁用租户的访问上下文。
        if (TenantStatus.DISABLED == tenant.getStatus()) {
            sessionCommandFacade.invalidateTenantSessions(tenant.getId().value(), "TENANT_DISABLED");
        }
        return TenantAssembler.toDto(tenant);
    }

    public TenantDTO getTenantByTenantId(TenantId tenantId) {
        return TenantAssembler.toDto(requireTenant(tenantId));
    }

    private Tenant requireTenant(TenantId tenantId) {
        return tenantRepository
                .findTenantById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId.value()));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
