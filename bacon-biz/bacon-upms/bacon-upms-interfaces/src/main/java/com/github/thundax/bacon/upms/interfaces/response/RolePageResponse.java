package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.RolePageResultDTO;
import java.util.List;

public record RolePageResponse(List<RoleResponse> records, long total, int pageNo, int pageSize) {

    public static RolePageResponse from(RolePageResultDTO dto) {
        List<RoleResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(RoleResponse::from).toList();
        return new RolePageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
