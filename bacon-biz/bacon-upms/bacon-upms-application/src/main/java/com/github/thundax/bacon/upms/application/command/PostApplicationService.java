package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.PostId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.PostDTO;
import com.github.thundax.bacon.upms.api.dto.PostPageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.PostPageResultDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostApplicationService {

    private final PostRepository postRepository;
    private final Ids ids;

    public PostApplicationService(PostRepository postRepository, Ids ids) {
        this.postRepository = postRepository;
        this.ids = ids;
    }

    public PostPageResultDTO pagePosts(PostPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        String tenantIdValue = query.getTenantId().value();
        return new PostPageResultDTO(
                postRepository.pagePosts(query.getTenantId(), query.getCode(), query.getName(),
                        query.getDepartmentId(), query.getStatus(), pageNo, pageSize).stream()
                        .map(post -> toDto(post, tenantIdValue))
                        .toList(),
                postRepository.countPosts(query.getTenantId(), query.getCode(), query.getName(),
                        query.getDepartmentId(), query.getStatus()),
                pageNo,
                pageSize
        );
    }

    public PostDTO getPostById(TenantId tenantId, PostId postId) {
        return toDto(requirePost(tenantId, postId));
    }

    @Transactional
    public PostDTO createPost(TenantId tenantId, String code, String name, String departmentId) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        return toDto(postRepository.save(new Post(ids.postId(), tenantId, normalize(code), normalize(name),
                toDepartmentId(departmentId), PostStatus.ENABLED)));
    }

    @Transactional
    public PostDTO updatePost(TenantId tenantId, PostId postId, String code, String name, String departmentId, String status) {
        Post currentPost = requirePost(tenantId, postId);
        validateRequired(code, "code");
        validateRequired(name, "name");
        return toDto(postRepository.save(new Post(
                currentPost.getId(),
                tenantId,
                normalize(code),
                normalize(name),
                toDepartmentId(departmentId),
                toPostStatus(status, currentPost.getStatus()),
                currentPost.getCreatedBy(),
                currentPost.getCreatedAt(),
                currentPost.getUpdatedBy(),
                currentPost.getUpdatedAt())));
    }

    @Transactional
    public void deletePost(TenantId tenantId, PostId postId) {
        requirePost(tenantId, postId);
        postRepository.delete(tenantId, postId);
    }

    private Post requirePost(TenantId tenantId, PostId postId) {
        return postRepository.findById(tenantId, postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
    }

    private PostDTO toDto(Post post) {
        return toDto(post, post.getTenantId().value());
    }

    private PostDTO toDto(Post post, String tenantIdValue) {
        return new PostDTO(post.getId() == null ? null : post.getId().value(), tenantIdValue, post.getCode(), post.getName(),
                post.getDepartmentId() == null ? null : post.getDepartmentId().value(),
                post.getStatus() == null ? null : post.getStatus().value());
    }

    private DepartmentId toDepartmentId(String departmentId) {
        return departmentId == null || departmentId.isBlank() ? null : DepartmentId.of(departmentId.trim());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private PostStatus toPostStatus(String value, PostStatus defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return PostStatus.fromValue(normalize(value).toUpperCase(Locale.ROOT));
    }

}
