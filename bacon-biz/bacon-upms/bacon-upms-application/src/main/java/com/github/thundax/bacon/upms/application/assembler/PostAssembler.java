package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.upms.api.dto.PostDTO;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.codec.PostCodeCodec;
import com.github.thundax.bacon.upms.application.codec.PostIdCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Post;

public final class PostAssembler {

    private PostAssembler() {}

    public static PostDTO toDto(Post post) {
        return new PostDTO(
                PostIdCodec.toValue(post.getId()),
                PostCodeCodec.toValue(post.getCode()),
                post.getName(),
                DepartmentIdCodec.toValue(post.getDepartmentId()),
                post.getStatus() == null ? null : post.getStatus().value());
    }
}
