package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.entity.Post;
import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Optional<Post> findById(Long tenantId, Long postId);

    List<Post> pagePosts(Long tenantId, String code, String name, Long departmentId, String status, int pageNo, int pageSize);

    long countPosts(Long tenantId, String code, String name, Long departmentId, String status);

    Post save(Post post);

    void delete(Long tenantId, Long postId);
}
