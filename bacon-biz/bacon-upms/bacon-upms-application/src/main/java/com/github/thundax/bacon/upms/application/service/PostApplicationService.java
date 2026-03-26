package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.upms.api.dto.PostDTO;
import com.github.thundax.bacon.upms.api.dto.PostPageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.PostPageResultDTO;
import com.github.thundax.bacon.upms.domain.entity.Post;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class PostApplicationService {

    private final PostRepository postRepository;

    public PostApplicationService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostPageResultDTO pagePosts(PostPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        return new PostPageResultDTO(
                postRepository.pagePosts(query.getTenantId(), query.getCode(), query.getName(),
                        query.getDepartmentId(), query.getStatus(), pageNo, pageSize).stream()
                        .map(this::toDto)
                        .toList(),
                postRepository.countPosts(query.getTenantId(), query.getCode(), query.getName(),
                        query.getDepartmentId(), query.getStatus()),
                pageNo,
                pageSize
        );
    }

    public PostDTO getPostById(Long tenantId, Long postId) {
        return toDto(requirePost(tenantId, postId));
    }

    public PostDTO createPost(Long tenantId, String code, String name, Long departmentId) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        return toDto(postRepository.save(new Post(null, tenantId, normalize(code), normalize(name),
                departmentId, "ENABLED")));
    }

    public PostDTO updatePost(Long tenantId, Long postId, String code, String name, Long departmentId, String status) {
        Post currentPost = requirePost(tenantId, postId);
        validateRequired(code, "code");
        validateRequired(name, "name");
        return toDto(postRepository.save(new Post(currentPost.getId(), currentPost.getCreatedBy(),
                currentPost.getCreatedAt(), currentPost.getUpdatedBy(), currentPost.getUpdatedAt(), tenantId,
                normalize(code), normalize(name), departmentId, normalizeNullable(status, currentPost.getStatus()))));
    }

    public void deletePost(Long tenantId, Long postId) {
        requirePost(tenantId, postId);
        postRepository.delete(tenantId, postId);
    }

    private Post requirePost(Long tenantId, Long postId) {
        return postRepository.findById(tenantId, postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
    }

    private PostDTO toDto(Post post) {
        return new PostDTO(post.getId(), post.getTenantId(), post.getCode(), post.getName(),
                post.getDepartmentId(), post.getStatus());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : normalize(value);
    }

}
