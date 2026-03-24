package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;

public record RoleResponse(Long id, Long tenantId, String code, String name, String roleType, String dataScopeType,
                           String status) {

    public static RoleResponse from(RoleDTO dto) {
        return new RoleResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(), dto.getRoleType(),
                dto.getDataScopeType(), dto.getStatus());
    }
}
