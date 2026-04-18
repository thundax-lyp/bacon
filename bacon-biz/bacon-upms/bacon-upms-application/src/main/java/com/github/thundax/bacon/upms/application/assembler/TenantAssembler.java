package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.application.codec.TenantCodeCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;

public final class TenantAssembler {

    private TenantAssembler() {}

    public static TenantDTO toDto(Tenant tenant) {
        return new TenantDTO(
                tenant.getId() == null ? null : tenant.getId().value(),
                tenant.getName(),
                TenantCodeCodec.toValue(tenant.getCode()),
                tenant.getStatus().value(),
                tenant.getExpiredAt());
    }
}
