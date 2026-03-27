package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.TenantPageResultDTO;
import java.util.List;

/**
 * 租户分页响应对象。
 */
public record TenantPageResponse(
        /** 当前页记录。 */
        List<TenantResponse> records,
        /** 总记录数。 */
        long total,
        /** 当前页码。 */
        int pageNo,
        /** 每页大小。 */
        int pageSize) {

    public static TenantPageResponse from(TenantPageResultDTO dto) {
        List<TenantResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(TenantResponse::from).toList();
        return new TenantPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
