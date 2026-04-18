package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Optional<Post> findById(PostId postId);

    List<Post> pagePosts(
            PostCode code, String name, DepartmentId departmentId, PostStatus status, int pageNo, int pageSize);

    long countPosts(PostCode code, String name, DepartmentId departmentId, PostStatus status);

    Post insert(Post post);

    Post update(Post post);

    void delete(PostId postId);
}
