package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.command.PostCommandApplicationService;
import com.github.thundax.bacon.upms.application.query.PostQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.assembler.PostInterfaceAssembler;
import com.github.thundax.bacon.upms.interfaces.request.PostCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.PostPageRequest;
import com.github.thundax.bacon.upms.interfaces.request.PostUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.PostPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@WrappedApiController
@RequestMapping("/upms/posts")
@Validated
@Tag(name = "UPMS-Post", description = "岗位管理接口")
public class PostController {

    private final PostCommandApplicationService postCommandApplicationService;
    private final PostQueryApplicationService postQueryApplicationService;

    public PostController(
            PostCommandApplicationService postCommandApplicationService,
            PostQueryApplicationService postQueryApplicationService) {
        this.postCommandApplicationService = postCommandApplicationService;
        this.postQueryApplicationService = postQueryApplicationService;
    }

    @Operation(summary = "分页查询岗位")
    @HasPermission("sys:post:view")
    @SysLog(module = "UPMS", action = "分页查询岗位", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public PostPageResponse page(@Valid @ModelAttribute PostPageRequest request) {
        return PostInterfaceAssembler.toPageResponse(
                postQueryApplicationService.page(PostInterfaceAssembler.toPageQuery(request)));
    }

    @Operation(summary = "按岗位 ID 查询岗位")
    @HasPermission("sys:post:view")
    @SysLog(module = "UPMS", action = "查询岗位详情", eventType = LogEventType.QUERY)
    @GetMapping("/{postId}")
    public PostResponse getPostById(
            @PathVariable("postId") @Positive(message = "postId must be greater than 0") Long postId) {
        return PostInterfaceAssembler.toResponse(
                postQueryApplicationService.getById(PostInterfaceAssembler.toPostId(postId)));
    }

    @Operation(summary = "创建岗位")
    @HasPermission("sys:post:create")
    @SysLog(module = "UPMS", action = "创建岗位", eventType = LogEventType.CREATE)
    @PostMapping
    public PostResponse createPost(@Valid @RequestBody PostCreateRequest request) {
        return PostInterfaceAssembler.toResponse(
                postCommandApplicationService.create(PostInterfaceAssembler.toCreateCommand(request)));
    }

    @Operation(summary = "修改岗位")
    @HasPermission("sys:post:update")
    @SysLog(module = "UPMS", action = "修改岗位", eventType = LogEventType.UPDATE)
    @PutMapping("/{postId}")
    public PostResponse updatePost(
            @PathVariable("postId") @Positive(message = "postId must be greater than 0") Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        return PostInterfaceAssembler.toResponse(
                postCommandApplicationService.update(PostInterfaceAssembler.toUpdateCommand(postId, request)));
    }

    @Operation(summary = "删除岗位")
    @HasPermission("sys:post:delete")
    @SysLog(module = "UPMS", action = "删除岗位", eventType = LogEventType.DELETE)
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable("postId") @Positive(message = "postId must be greater than 0") Long postId) {
        postCommandApplicationService.delete(PostInterfaceAssembler.toPostId(postId));
    }
}
