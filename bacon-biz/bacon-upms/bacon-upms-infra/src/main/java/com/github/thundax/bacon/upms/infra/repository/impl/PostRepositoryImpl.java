package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class PostRepositoryImpl implements PostRepository {

    private final PostPersistenceSupport support;

    public PostRepositoryImpl(PostPersistenceSupport support) {
        this.support = support;
    }

    @Override
    public Optional<Post> findById(PostId postId) {
        return support.findPostById(postId);
    }

    @Override
    public List<Post> pagePosts(
            String code, String name, DepartmentId departmentId, PostStatus status, int pageNo, int pageSize) {
        return support.listPosts(code, name, departmentId, status, pageNo, pageSize);
    }

    @Override
    public long countPosts(String code, String name, DepartmentId departmentId, PostStatus status) {
        return support.countPosts(code, name, departmentId, status);
    }

    @Override
    public Post insert(Post post) {
        return support.insertPost(post);
    }

    @Override
    public Post update(Post post) {
        return support.updatePost(post);
    }

    @Override
    public void delete(PostId postId) {
        support.deletePost(postId);
    }
}
