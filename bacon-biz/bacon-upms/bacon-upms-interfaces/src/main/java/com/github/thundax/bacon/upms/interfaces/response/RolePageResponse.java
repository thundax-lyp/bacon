package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.result.PageResult;
import java.util.List;

/**
 * 角色分页响应对象。
 */
public record RolePageResponse(
        /** 当前页记录。 */
        List<RoleResponse> records,
        /** 总记录数。 */
        long total,
        /** 当前页码。 */
        int pageNo,
        /** 每页大小。 */
        int pageSize) {

    public static RolePageResponse from(PageResult<RoleDTO> dto) {
        List<RoleResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(RoleResponse::from).toList();
        return new RolePageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
