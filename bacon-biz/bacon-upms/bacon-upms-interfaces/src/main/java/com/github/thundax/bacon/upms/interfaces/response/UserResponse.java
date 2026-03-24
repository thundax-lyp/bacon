package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserDTO;

public record UserResponse(Long id, Long tenantId, String account, String name, String phone, Long departmentId,
                           String status, boolean deleted) {

    public static UserResponse from(UserDTO dto) {
        return new UserResponse(dto.getId(), dto.getTenantId(), dto.getAccount(), dto.getName(), dto.getPhone(),
                dto.getDepartmentId(), dto.getStatus(), dto.isDeleted());
    }
}
