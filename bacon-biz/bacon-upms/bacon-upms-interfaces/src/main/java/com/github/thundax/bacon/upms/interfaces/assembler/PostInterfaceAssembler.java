package com.github.thundax.bacon.upms.interfaces.assembler;

import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.codec.PostCodeCodec;
import com.github.thundax.bacon.upms.application.codec.PostIdCodec;
import com.github.thundax.bacon.upms.application.command.PostCreateCommand;
import com.github.thundax.bacon.upms.application.command.PostUpdateCommand;
import com.github.thundax.bacon.upms.application.dto.PostDTO;
import com.github.thundax.bacon.upms.application.query.PostPageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.interfaces.request.PostCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.PostPageRequest;
import com.github.thundax.bacon.upms.interfaces.request.PostUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.PostPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.PostResponse;

public final class PostInterfaceAssembler {

    private PostInterfaceAssembler() {}

    public static PostPageQuery toPageQuery(PostPageRequest request) {
        return new PostPageQuery(
                PostCodeCodec.toDomain(request.getCode()),
                request.getName(),
                DepartmentIdCodec.toDomain(request.getDepartmentId()),
                request.getStatus() == null ? null : PostStatus.from(request.getStatus()),
                request.getPageNo(),
                request.getPageSize());
    }

    public static PostCreateCommand toCreateCommand(PostCreateRequest request) {
        return new PostCreateCommand(
                PostCodeCodec.toDomain(request.code()),
                request.name(),
                DepartmentIdCodec.toDomain(request.departmentId()));
    }

    public static PostUpdateCommand toUpdateCommand(Long postId, PostUpdateRequest request) {
        return new PostUpdateCommand(
                PostIdCodec.toDomain(postId),
                PostCodeCodec.toDomain(request.code()),
                request.name(),
                DepartmentIdCodec.toDomain(request.departmentId()),
                request.status() == null || request.status().isBlank() ? null : PostStatus.from(request.status()));
    }

    public static PostId toPostId(Long postId) {
        return PostIdCodec.toDomain(postId);
    }

    public static PostResponse toResponse(PostDTO dto) {
        return PostResponse.from(dto);
    }

    public static PostPageResponse toPageResponse(PageResult<PostDTO> pageResult) {
        return PostPageResponse.from(pageResult);
    }
}
