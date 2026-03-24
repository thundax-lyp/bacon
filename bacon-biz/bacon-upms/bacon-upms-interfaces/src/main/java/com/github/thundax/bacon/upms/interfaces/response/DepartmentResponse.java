package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;

public record DepartmentResponse(Long id, Long tenantId, String code, String name, Long parentId, Long leaderUserId,
                                 String status) {

    public static DepartmentResponse from(DepartmentDTO dto) {
        return new DepartmentResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(), dto.getParentId(),
                dto.getLeaderUserId(), dto.getStatus());
    }
}
