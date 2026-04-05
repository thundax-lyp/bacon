package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.PostDTO;
import java.util.List;

/**
 * 岗位分页响应对象。
 */
public record PostPageResponse(
        /** 当前页记录。 */
        List<PostResponse> records,
        /** 总记录数。 */
        long total,
        /** 当前页码。 */
        int pageNo,
        /** 每页大小。 */
        int pageSize) {

    public static PostPageResponse from(PageResultDTO<PostDTO> dto) {
        List<PostResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(PostResponse::from).toList();
        return new PostPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
