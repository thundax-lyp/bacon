package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.ResourcePageResultDTO;
import java.util.List;

public record ResourcePageResponse(List<ResourceResponse> records, long total, int pageNo, int pageSize) {

    public static ResourcePageResponse from(ResourcePageResultDTO dto) {
        List<ResourceResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(ResourceResponse::from).toList();
        return new ResourcePageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
