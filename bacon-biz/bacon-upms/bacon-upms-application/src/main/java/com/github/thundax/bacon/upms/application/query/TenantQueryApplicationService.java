package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.result.PageResult;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.application.assembler.TenantAssembler;
import com.github.thundax.bacon.upms.application.dto.TenantDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantQueryApplicationService {

    private final TenantRepository tenantRepository;

    public TenantQueryApplicationService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public PageResult<TenantDTO> page(TenantPageQuery query) {
        int normalizedPageNo = query.getPageNo();
        int normalizedPageSize = query.getPageSize();
        return new PageResult<>(
                tenantRepository
                        .page(query.getName(), query.getStatus(), normalizedPageNo, normalizedPageSize)
                        .stream()
                        .map(TenantAssembler::toDto)
                        .toList(),
                tenantRepository.count(query.getName(), query.getStatus()),
                normalizedPageNo,
                normalizedPageSize);
    }

    public TenantDTO getById(TenantId tenantId) {
        return TenantAssembler.toDto(requireTenant(tenantId));
    }

    private Tenant requireTenant(TenantId tenantId) {
        return tenantRepository
                .findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantId.value()));
    }
}
