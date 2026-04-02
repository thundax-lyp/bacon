package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.PostId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.PostDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.PostMapper;
import java.time.LocalDateTime;
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

    Optional<Post> findPostById(TenantId tenantId, PostId postId) {
        return Optional.ofNullable(postMapper.selectOne(Wrappers.<PostDO>lambdaQuery()
                        .eq(PostDO::getTenantId, tenantId)
                        .eq(PostDO::getId, postId)))
                .map(this::toDomain);
    }

    List<Post> listPosts(TenantId tenantId, String code, String name, DepartmentId departmentId, String status, int pageNo,
                         int pageSize) {
        return postMapper.selectList(Wrappers.<PostDO>lambdaQuery()
                        .eq(tenantId != null, PostDO::getTenantId, tenantId)
                        .like(hasText(code), PostDO::getCode, code)
                        .like(hasText(name), PostDO::getName, name)
                        .eq(departmentId != null, PostDO::getDepartmentId, departmentId)
                        .eq(hasText(status), PostDO::getStatus, trim(status))
                        .orderByAsc(PostDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    long countPosts(TenantId tenantId, String code, String name, DepartmentId departmentId, String status) {
        return Optional.ofNullable(postMapper.selectCount(Wrappers.<PostDO>lambdaQuery()
                        .eq(tenantId != null, PostDO::getTenantId, tenantId)
                        .like(hasText(code), PostDO::getCode, code)
                        .like(hasText(name), PostDO::getName, name)
                        .eq(departmentId != null, PostDO::getDepartmentId, departmentId)
                        .eq(hasText(status), PostDO::getStatus, trim(status))))
                .orElse(0L);
    }

    Post savePost(Post post) {
        PostDO dataObject = toDataObject(post);
        LocalDateTime now = LocalDateTime.now();
        if (dataObject.getId() == null) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            postMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            postMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    void deletePost(TenantId tenantId, PostId postId) {
        postMapper.delete(Wrappers.<PostDO>lambdaQuery()
                .eq(PostDO::getTenantId, tenantId)
                .eq(PostDO::getId, postId));
    }
}
