package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final Map<String, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong postIdSequence = new AtomicLong(6002L);

    @Override
    public Optional<Post> findById(Long tenantId, Long postId) {
        return Optional.ofNullable(posts.get(UpmsRepositoryHelper.postKey(tenantId, postId)));
    }

    @Override
    public List<Post> pagePosts(Long tenantId, String code, String name, Long departmentId, String status,
                                int pageNo, int pageSize) {
        return filteredPosts(tenantId, code, name, departmentId, status).stream()
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    @Override
    public long countPosts(Long tenantId, String code, String name, Long departmentId, String status) {
        return filteredPosts(tenantId, code, name, departmentId, status).size();
    }

    @Override
    public Post save(Post post) {
        Long postId = post.getId() == null ? postIdSequence.getAndIncrement() : post.getId();
        Post savedPost = post.getId() == null
                ? new Post(postId, post.getTenantId(), post.getCode(), post.getName(),
                post.getDepartmentId(), post.getStatus())
                : post;
        posts.put(UpmsRepositoryHelper.postKey(savedPost.getTenantId(), savedPost.getId()), savedPost);
        return savedPost;
    }

    @Override
    public void delete(Long tenantId, Long postId) {
        posts.remove(UpmsRepositoryHelper.postKey(tenantId, postId));
    }

    private List<Post> filteredPosts(Long tenantId, String code, String name, Long departmentId, String status) {
        return posts.values().stream()
                .filter(post -> tenantId == null || tenantId.equals(post.getTenantId()))
                .filter(post -> code == null || post.getCode().contains(code))
                .filter(post -> name == null || post.getName().contains(name))
                .filter(post -> departmentId == null || departmentId.equals(post.getDepartmentId()))
                .filter(post -> status == null || status.equalsIgnoreCase(post.getStatus()))
                .sorted(Comparator.comparing(Post::getId))
                .toList();
    }
}
