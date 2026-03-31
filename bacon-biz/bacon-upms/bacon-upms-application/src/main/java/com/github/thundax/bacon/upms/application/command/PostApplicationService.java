package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.PostDTO;
import com.github.thundax.bacon.upms.api.dto.PostPageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.PostPageResultDTO;
import com.github.thundax.bacon.upms.api.enums.UpmsStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class PostApplicationService {

    private final PostRepository postRepository;
    private final TenantRepository tenantRepository;

    public PostApplicationService(PostRepository postRepository, TenantRepository tenantRepository) {
        this.postRepository = postRepository;
        this.tenantRepository = tenantRepository;
    }

    public PostPageResultDTO pagePosts(PostPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        String tenantNo = resolveTenantNoByTenantId(query.getTenantId());
        return new PostPageResultDTO(
                postRepository.pagePosts(query.getTenantId(), query.getCode(), query.getName(),
                        query.getDepartmentId(), query.getStatus(), pageNo, pageSize).stream()
                        .map(post -> toDto(post, tenantNo))
                        .toList(),
                postRepository.countPosts(query.getTenantId(), query.getCode(), query.getName(),
                        query.getDepartmentId(), query.getStatus()),
                pageNo,
                pageSize
        );
    }

    public PostDTO getPostById(TenantId tenantId, Long postId) {
        return toDto(requirePost(tenantId, postId));
    }

    public PostDTO createPost(TenantId tenantId, String code, String name, Long departmentId) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        return toDto(postRepository.save(new Post(null, tenantId, normalize(code), normalize(name),
                departmentId, UpmsStatusEnum.ENABLED.value())));
    }

    public PostDTO updatePost(TenantId tenantId, Long postId, String code, String name, Long departmentId, String status) {
        Post currentPost = requirePost(tenantId, postId);
        validateRequired(code, "code");
        validateRequired(name, "name");
        return toDto(postRepository.save(new Post(
                currentPost.getId(),
                tenantId,
                normalize(code),
                normalize(name),
                departmentId,
                normalizeNullable(status, currentPost.getStatus()),
                currentPost.getCreatedBy(),
                currentPost.getCreatedAt(),
                currentPost.getUpdatedBy(),
                currentPost.getUpdatedAt())));
    }

    public void deletePost(TenantId tenantId, Long postId) {
        requirePost(tenantId, postId);
        postRepository.delete(tenantId, postId);
    }

    private Post requirePost(TenantId tenantId, Long postId) {
        return postRepository.findById(tenantId, postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
    }

    private PostDTO toDto(Post post) {
        return toDto(post, resolveTenantNoByTenantId(post.getTenantId()));
    }

    private PostDTO toDto(Post post, String tenantNo) {
        return new PostDTO(post.getId(), tenantNo, post.getCode(), post.getName(),
                post.getDepartmentId(), post.getStatus());
    }

    private String resolveTenantNoByTenantId(TenantId tenantId) {
        return tenantRepository.findTenantById(tenantId)
                .map(tenant -> tenant.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId.value()));
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
