package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.PostDTO;
import com.github.thundax.bacon.upms.api.dto.PostPageQueryDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostApplicationService {

    private static final String POST_ID_BIZ_TAG = "post-id";

    private final PostRepository postRepository;
    private final IdGenerator idGenerator;

    public PostApplicationService(PostRepository postRepository, IdGenerator idGenerator) {
        this.postRepository = postRepository;
        this.idGenerator = idGenerator;
    }

    public PageResultDTO<PostDTO> pagePosts(PostPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        Long tenantIdValue = query.getTenantId().value();
        return new PageResultDTO<>(
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
        DepartmentId domainDepartmentId = toDepartmentId(departmentId);
        return toDto(postRepository.save(new Post(
                idGenerator.nextId(POST_ID_BIZ_TAG),
                tenantId.value(),
                normalize(code),
                normalize(name),
                domainDepartmentId == null ? null : domainDepartmentId.value(),
                PostStatus.ENABLED,
                null,
                null,
                null,
                null)));
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

    private PostDTO toDto(Post post, Long tenantIdValue) {
        return new PostDTO(post.getId() == null ? null : post.getId().value(), tenantIdValue, post.getCode(), post.getName(),
                post.getDepartmentId() == null ? null : post.getDepartmentId().value(),
                post.getStatus() == null ? null : post.getStatus().value());
    }

    private DepartmentId toDepartmentId(String departmentId) {
        return departmentId == null || departmentId.isBlank() ? null : DepartmentId.of(Long.parseLong(departmentId.trim()));
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
        return PostStatus.from(normalize(value).toUpperCase(Locale.ROOT));
    }

}
