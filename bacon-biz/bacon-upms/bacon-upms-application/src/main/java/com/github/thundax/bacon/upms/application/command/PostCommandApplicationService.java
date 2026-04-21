package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.upms.application.assembler.PostAssembler;
import com.github.thundax.bacon.upms.application.codec.PostIdCodec;
import com.github.thundax.bacon.upms.application.dto.PostDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Post;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostCommandApplicationService {

    private static final String POST_ID_BIZ_TAG = "post-id";

    private final PostRepository postRepository;
    private final IdGenerator idGenerator;

    public PostCommandApplicationService(PostRepository postRepository, IdGenerator idGenerator) {
        this.postRepository = postRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public PostDTO create(PostCreateCommand command) {
        validateRequired(command.name(), "name");
        if (command.code() == null) {
            throw new BadRequestException("code must not be null");
        }
        return PostAssembler.toDto(postRepository.insert(Post.create(
                PostIdCodec.toDomain(idGenerator.nextId(POST_ID_BIZ_TAG)),
                command.code(),
                trimPreservingNull(command.name()),
                command.departmentId())));
    }

    @Transactional
    public PostDTO update(PostUpdateCommand command) {
        Post currentPost = requirePost(command.postId());
        validateRequired(command.name(), "name");
        if (command.code() == null) {
            throw new BadRequestException("code must not be null");
        }
        currentPost.recodeAs(command.code());
        currentPost.rename(trimPreservingNull(command.name()));
        currentPost.assignDepartment(command.departmentId());
        if (command.status() != null) {
            updateStatus(currentPost, command.status());
        }
        return PostAssembler.toDto(postRepository.update(currentPost));
    }

    @Transactional
    public void delete(PostId postId) {
        requirePost(postId);
        postRepository.delete(postId);
    }

    private void updateStatus(Post post, PostStatus status) {
        if (status == PostStatus.ENABLED) {
            post.enable();
        } else {
            post.disable();
        }
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
