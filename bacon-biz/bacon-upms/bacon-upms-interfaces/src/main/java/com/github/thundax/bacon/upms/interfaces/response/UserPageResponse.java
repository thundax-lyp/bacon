package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.application.result.PageResult;
import java.util.List;

/**
 * 用户分页响应对象。
 */
public record UserPageResponse(
        /** 当前页记录。 */
        List<UserResponse> records,
        /** 总记录数。 */
        long total,
        /** 当前页码。 */
        int pageNo,
        /** 每页大小。 */
        int pageSize) {

    public static UserPageResponse from(PageResult<UserDTO> dto) {
        List<UserResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(UserResponse::from).toList();
        return new UserPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
