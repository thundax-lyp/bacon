package com.github.thundax.bacon.storage.interfaces.response;

import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import java.util.List;

public record StoredObjectPageResponse(List<StoredObjectResponse> records, long total, int pageNo, int pageSize) {

    public static StoredObjectPageResponse from(PageResult<StoredObjectDTO> dto) {
        List<StoredObjectResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(StoredObjectResponse::from).toList();
        return new StoredObjectPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
