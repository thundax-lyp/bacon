package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.application.dto.SysLogDTO;
import com.github.thundax.bacon.common.application.page.PageResult;
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

    public static SysLogPageResponse from(PageResult<SysLogDTO> dto) {
        List<SysLogResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(SysLogResponse::from).toList();
        return new SysLogPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
