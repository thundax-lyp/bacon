package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.PostDO;

public final class PostPersistenceAssembler {

    private PostPersistenceAssembler() {}

    public static PostDO toDataObject(Post post) {
        return new PostDO(
                post.getId(),
                post.getTenantId(),
                post.getCode(),
                post.getName(),
                post.getDepartmentId(),
                post.getStatus() == null ? null : post.getStatus().value());
    }

    public static Post toDomain(PostDO dataObject) {
        return Post.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getCode(),
                dataObject.getName(),
                dataObject.getDepartmentId(),
                PostStatus.from(dataObject.getStatus()));
    }
}
