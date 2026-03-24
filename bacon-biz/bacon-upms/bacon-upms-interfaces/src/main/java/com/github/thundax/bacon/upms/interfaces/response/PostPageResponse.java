package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.PostPageResultDTO;
import java.util.List;

public record PostPageResponse(List<PostResponse> records, long total, int pageNo, int pageSize) {

    public static PostPageResponse from(PostPageResultDTO dto) {
        List<PostResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(PostResponse::from).toList();
        return new PostPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
