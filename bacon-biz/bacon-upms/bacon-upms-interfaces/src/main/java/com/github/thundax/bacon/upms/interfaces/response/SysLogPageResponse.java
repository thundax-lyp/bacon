package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.SysLogPageResultDTO;
import java.util.List;

public record SysLogPageResponse(List<SysLogResponse> records, long total, int pageNo, int pageSize) {

    public static SysLogPageResponse from(SysLogPageResultDTO dto) {
        List<SysLogResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(SysLogResponse::from).toList();
        return new SysLogPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
