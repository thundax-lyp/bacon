package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateTenantFacadeRequest;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.application.assembler.TenantAssembler;
import com.github.thundax.bacon.upms.application.dto.TenantDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantCommandApplicationService {

    private static final String TENANT_ID_BIZ_TAG = "tenant-id";

    private final TenantRepository tenantRepository;
    private final SessionCommandFacade sessionCommandFacade;
    private final IdGenerator idGenerator;

    public TenantCommandApplicationService(
            TenantRepository tenantRepository, SessionCommandFacade sessionCommandFacade, IdGenerator idGenerator) {
        this.tenantRepository = tenantRepository;
        this.sessionCommandFacade = sessionCommandFacade;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public TenantDTO create(TenantCreateCommand command) {
        validateRequired(command.name(), "name");
        if (command.code() == null) {
            throw new BadRequestException("code must not be null");
        }
        TenantId tenantId = TenantId.of(idGenerator.nextId(TENANT_ID_BIZ_TAG));
        tenantRepository.findByCode(command.code()).ifPresent(tenant -> {
            throw new ConflictException("Tenant code already exists: " + command.code().value());
        });
        return TenantAssembler.toDto(tenantRepository.insert(Tenant.create(
                tenantId, command.name().trim(), command.code(), command.expiredAt())));
    }

    @Transactional
    public TenantDTO update(TenantUpdateCommand command) {
        validateRequired(command.name(), "name");
        if (command.code() == null) {
            throw new BadRequestException("code must not be null");
        }
        Tenant currentTenant = requireTenant(command.tenantId());
        tenantRepository
                .findByCode(command.code())
                .filter(tenant -> !tenant.getId().equals(command.tenantId()))
                .ifPresent(tenant -> {
                    throw new ConflictException("Tenant code already exists: " + command.code().value());
                });
        currentTenant.rename(command.name().trim());
        currentTenant.recodeAs(command.code());
        if (command.expiredAt() == null) {
            currentTenant.clearExpiry();
        } else {
            currentTenant.renewTo(command.expiredAt());
        }
        return TenantAssembler.toDto(tenantRepository.update(currentTenant));
    }

    @Transactional
    public TenantDTO updateStatus(TenantStatusUpdateCommand command) {
        if (command.status() == null) {
            throw new BadRequestException("status must not be null");
        }
        Tenant tenant = requireTenant(command.tenantId());
        if (TenantStatus.ACTIVE == command.status()) {
            tenant.activate();
        } else {
            tenant.disable();
        }
        tenant = tenantRepository.update(tenant);
        // 租户停用要同步踢出该租户下所有会话，否则鉴权缓存里仍会保留已禁用租户的访问上下文。
        if (TenantStatus.DISABLED == tenant.getStatus()) {
            sessionCommandFacade.invalidateTenantSessions(
                    new SessionInvalidateTenantFacadeRequest(tenant.getId().value(), "TENANT_DISABLED"));
        }
        return TenantAssembler.toDto(tenant);
    }

    private Tenant requireTenant(TenantId tenantId) {
        return tenantRepository
                .findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantId.value()));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
    }
}
