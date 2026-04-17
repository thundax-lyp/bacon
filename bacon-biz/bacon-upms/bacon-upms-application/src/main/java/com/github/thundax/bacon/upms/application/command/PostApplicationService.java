package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.PostDTO;
import com.github.thundax.bacon.upms.application.assembler.PostAssembler;
import com.github.thundax.bacon.upms.application.codec.PostIdCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
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

    public PageResultDTO<PostDTO> pagePosts(
            String code, String name, DepartmentId departmentId, PostStatus status, Integer pageNo, Integer pageSize) {
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        return new PageResultDTO<>(
                postRepository
                        .pagePosts(code, name, departmentId, status, normalizedPageNo, normalizedPageSize)
                        .stream()
                        .map(PostAssembler::toDto)
                        .toList(),
                postRepository.countPosts(code, name, departmentId, status),
                normalizedPageNo,
                normalizedPageSize);
    }

    public PostDTO getPostById(PostId postId) {
        return PostAssembler.toDto(requirePost(postId));
    }

    @Transactional
    public PostDTO createPost(String code, String name, DepartmentId departmentId) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        return PostAssembler.toDto(postRepository.insert(Post.create(
                PostIdCodec.toDomain(idGenerator.nextId(POST_ID_BIZ_TAG)),
                trimPreservingNull(code),
                trimPreservingNull(name),
                departmentId,
                PostStatus.ENABLED)));
    }

    @Transactional
    public PostDTO updatePost(PostId postId, String code, String name, DepartmentId departmentId, PostStatus status) {
        Post currentPost = requirePost(postId);
        validateRequired(code, "code");
        validateRequired(name, "name");
        return PostAssembler.toDto(postRepository.update(currentPost.update(
                trimPreservingNull(code),
                trimPreservingNull(name),
                departmentId,
                status == null ? currentPost.getStatus() : status)));
    }

    @Transactional
    public void deletePost(PostId postId) {
        requirePost(postId);
        postRepository.delete(postId);
    }

    private Post requirePost(PostId postId) {
        return postRepository
                .findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
    }

    private String trimPreservingNull(String value) {
        return value == null ? null : value.trim();
    }
}
