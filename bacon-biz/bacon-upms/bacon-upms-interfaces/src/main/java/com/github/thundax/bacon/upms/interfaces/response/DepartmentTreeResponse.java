package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.DepartmentTreeDTO;
import java.util.List;

public record DepartmentTreeResponse(Long id, Long tenantId, String code, String name, Long parentId,
                                     Long leaderUserId, String status, List<DepartmentTreeResponse> children) {

    public static DepartmentTreeResponse from(DepartmentTreeDTO dto) {
        List<DepartmentTreeResponse> childResponses = dto.getChildren() == null
                ? List.of()
                : dto.getChildren().stream().map(DepartmentTreeResponse::from).toList();
        return new DepartmentTreeResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(), dto.getParentId(),
                dto.getLeaderUserId(), dto.getStatus(), childResponses);
    }
}
