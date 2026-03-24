package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserPageResultDTO;
import java.util.List;

public record UserPageResponse(List<UserResponse> records, long total, int pageNo, int pageSize) {

    public static UserPageResponse from(UserPageResultDTO dto) {
        List<UserResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(UserResponse::from).toList();
        return new UserPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
