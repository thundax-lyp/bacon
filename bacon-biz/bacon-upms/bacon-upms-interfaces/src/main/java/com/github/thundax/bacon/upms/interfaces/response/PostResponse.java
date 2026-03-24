package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.PostDTO;

public record PostResponse(Long id, Long tenantId, String code, String name, Long departmentId, String status) {

    public static PostResponse from(PostDTO dto) {
        return new PostResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(),
                dto.getDepartmentId(), dto.getStatus());
    }
}
