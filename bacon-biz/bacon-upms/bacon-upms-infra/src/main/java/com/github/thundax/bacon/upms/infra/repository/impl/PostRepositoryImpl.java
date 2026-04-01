package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final PostPersistenceSupport support;

    public PostRepositoryImpl(PostPersistenceSupport support) {
        this.support = support;
    }

    @Override
    public Optional<Post> findById(TenantId tenantId, Long postId) {
        return support.findPostById(tenantId, postId);
    }

    @Override
    public List<Post> pagePosts(TenantId tenantId, String code, String name, DepartmentId departmentId, String status,
                                int pageNo, int pageSize) {
        return support.listPosts(tenantId, code, name, departmentId, status, pageNo, pageSize);
    }

    @Override
    public long countPosts(TenantId tenantId, String code, String name, DepartmentId departmentId, String status) {
        return support.countPosts(tenantId, code, name, departmentId, status);
    }

    @Override
    public Post save(Post post) {
        return support.savePost(post);
    }

    @Override
    public void delete(TenantId tenantId, Long postId) {
        support.deletePost(tenantId, postId);
    }
}
