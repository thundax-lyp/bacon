package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Optional<Post> findById(TenantId tenantId, Long postId);

    List<Post> pagePosts(TenantId tenantId, String code, String name, DepartmentId departmentId, String status, int pageNo, int pageSize);

    long countPosts(TenantId tenantId, String code, String name, DepartmentId departmentId, String status);

    Post save(Post post);

    void delete(TenantId tenantId, Long postId);
}
