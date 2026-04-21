package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.result.PageResult;
import com.github.thundax.bacon.upms.application.assembler.PostAssembler;
import com.github.thundax.bacon.upms.application.dto.PostDTO;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class PostQueryApplicationService {

    private final PostRepository postRepository;

    public PostQueryApplicationService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PageResult<PostDTO> page(PostPageQuery query) {
        int normalizedPageNo = query.getPageNo();
        int normalizedPageSize = query.getPageSize();
        return new PageResult<>(
                postRepository
                        .page(
                                query.getCode(),
                                query.getName(),
                                query.getDepartmentId(),
                                query.getStatus(),
                                normalizedPageNo,
                                normalizedPageSize)
                        .stream()
                        .map(PostAssembler::toDto)
                        .toList(),
                postRepository.count(query.getCode(), query.getName(), query.getDepartmentId(), query.getStatus()),
                normalizedPageNo,
                normalizedPageSize);
    }

    public PostDTO getById(PostId postId) {
        return PostAssembler.toDto(
                postRepository
                        .findById(postId)
                        .orElseThrow(() -> new NotFoundException("Post not found: " + postId)));
    }
}
