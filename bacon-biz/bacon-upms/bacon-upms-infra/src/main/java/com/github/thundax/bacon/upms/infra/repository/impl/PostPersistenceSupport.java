package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.PostPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.PostDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.PostMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class PostPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final PostMapper postMapper;

    PostPersistenceSupport(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    Optional<Post> findPostById(PostId postId) {
        requireTenantId();
        return Optional.ofNullable(
                        postMapper.selectOne(Wrappers.<PostDO>lambdaQuery().eq(PostDO::getId, postId)))
                .map(PostPersistenceAssembler::toDomain);
    }

    List<Post> listPosts(String code, String name, DepartmentId departmentId, String status, int pageNo, int pageSize) {
        return postMapper
                .selectList(Wrappers.<PostDO>lambdaQuery()
                        .like(hasText(code), PostDO::getCode, code)
                        .like(hasText(name), PostDO::getName, name)
                        .eq(departmentId != null, PostDO::getDepartmentId, departmentId)
                        .eq(hasText(status), PostDO::getStatus, trim(status))
                        .orderByAsc(PostDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(PostPersistenceAssembler::toDomain)
                .toList();
    }

    long countPosts(String code, String name, DepartmentId departmentId, String status) {
        return Optional.ofNullable(postMapper.selectCount(Wrappers.<PostDO>lambdaQuery()
                        .like(hasText(code), PostDO::getCode, code)
                        .like(hasText(name), PostDO::getName, name)
                        .eq(departmentId != null, PostDO::getDepartmentId, departmentId)
                        .eq(hasText(status), PostDO::getStatus, trim(status))))
                .orElse(0L);
    }

    Post savePost(Post post) {
        PostDO dataObject = PostPersistenceAssembler.toDataObject(post);
        if (dataObject.getId() == null) {
            postMapper.insert(dataObject);
        } else {
            postMapper.updateById(dataObject);
        }
        return PostPersistenceAssembler.toDomain(dataObject);
    }

    void deletePost(PostId postId) {
        requireTenantId();
        postMapper.delete(Wrappers.<PostDO>lambdaQuery().eq(PostDO::getId, postId));
    }

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
