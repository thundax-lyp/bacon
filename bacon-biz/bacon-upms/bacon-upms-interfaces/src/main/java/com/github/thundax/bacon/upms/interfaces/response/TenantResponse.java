package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;

public record TenantResponse(Long id, Long tenantId, String code, String name, String status) {

    public static TenantResponse from(TenantDTO dto) {
        return new TenantResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(), dto.getStatus());
    }
}
