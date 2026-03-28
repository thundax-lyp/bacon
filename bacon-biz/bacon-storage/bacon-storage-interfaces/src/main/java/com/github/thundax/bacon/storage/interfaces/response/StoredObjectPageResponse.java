package com.github.thundax.bacon.storage.interfaces.response;

import com.github.thundax.bacon.storage.api.dto.StoredObjectPageResultDTO;

import java.util.List;

public record StoredObjectPageResponse(
        List<StoredObjectResponse> records,
        long total,
        int pageNo,
        int pageSize) {

    public static StoredObjectPageResponse from(StoredObjectPageResultDTO dto) {
        List<StoredObjectResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(StoredObjectResponse::from).toList();
        return new StoredObjectPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
