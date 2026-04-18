package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.PostDO;

public final class PostPersistenceAssembler {

    private PostPersistenceAssembler() {}

    public static PostDO toDataObject(Post post) {
        return new PostDO(
                post.getId() == null ? null : post.getId().value(),
                BaconContextHolder.requireTenantId(),
                post.getCode() == null ? null : post.getCode().value(),
                post.getName(),
                post.getDepartmentId() == null ? null : post.getDepartmentId().value(),
                post.getStatus() == null ? null : post.getStatus().value());
    }

    public static Post toDomain(PostDO dataObject) {
        return Post.reconstruct(
                dataObject.getId() == null ? null : PostId.of(dataObject.getId()),
                dataObject.getCode() == null ? null : PostCode.of(dataObject.getCode()),
                dataObject.getName(),
                dataObject.getDepartmentId() == null ? null : DepartmentId.of(dataObject.getDepartmentId()),
                PostStatus.from(dataObject.getStatus()));
    }
}
