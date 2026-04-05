package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.SysLogDTO;
import java.util.List;

/**
 * 系统日志分页响应对象。
 */
public record SysLogPageResponse(
        /** 当前页记录。 */
        List<SysLogResponse> records,
        /** 总记录数。 */
        long total,
        /** 当前页码。 */
        int pageNo,
        /** 每页大小。 */
        int pageSize) {

    public static SysLogPageResponse from(PageResultDTO<SysLogDTO> dto) {
        List<SysLogResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(SysLogResponse::from).toList();
        return new SysLogPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
