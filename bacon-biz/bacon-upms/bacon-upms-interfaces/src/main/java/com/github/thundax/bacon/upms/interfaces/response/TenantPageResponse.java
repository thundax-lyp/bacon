package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.TenantPageResultDTO;
import java.util.List;

public record TenantPageResponse(List<TenantResponse> records, long total, int pageNo, int pageSize) {

    public static TenantPageResponse from(TenantPageResultDTO dto) {
        List<TenantResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(TenantResponse::from).toList();
        return new TenantPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
