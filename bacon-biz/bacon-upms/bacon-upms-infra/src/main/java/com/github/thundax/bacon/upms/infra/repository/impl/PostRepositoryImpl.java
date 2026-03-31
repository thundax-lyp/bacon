package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(UpmsRepositorySupport.class)
public class PostRepositoryImpl implements PostRepository {

    private final UpmsRepositorySupport support;

    public PostRepositoryImpl(UpmsRepositorySupport support) {
        this.support = support;
    }

    @Override
    public Optional<Post> findById(Long tenantId, Long postId) {
        return support.findPostById(tenantId, postId);
    }

    @Override
    public List<Post> pagePosts(Long tenantId, String code, String name, Long departmentId, String status,
                                int pageNo, int pageSize) {
        return support.listPosts(tenantId, code, name, departmentId, status, pageNo, pageSize);
    }

    @Override
    public long countPosts(Long tenantId, String code, String name, Long departmentId, String status) {
        return support.countPosts(tenantId, code, name, departmentId, status);
    }

    @Override
    public Post save(Post post) {
        return support.savePost(post);
    }

    @Override
    public void delete(Long tenantId, Long postId) {
        support.deletePost(tenantId, postId);
    }
}
