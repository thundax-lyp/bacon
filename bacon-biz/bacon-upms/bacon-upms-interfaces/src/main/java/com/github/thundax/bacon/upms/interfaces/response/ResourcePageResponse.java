package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.ResourceDTO;
import java.util.List;

/**
 * 资源分页响应对象。
 */
public record ResourcePageResponse(
        /** 当前页记录。 */
        List<ResourceResponse> records,
        /** 总记录数。 */
        long total,
        /** 当前页码。 */
        int pageNo,
        /** 每页大小。 */
        int pageSize) {

    public static ResourcePageResponse from(PageResultDTO<ResourceDTO> dto) {
        List<ResourceResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(ResourceResponse::from).toList();
        return new ResourcePageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
