package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.api.dto.PostPageQueryDTO;
import com.github.thundax.bacon.upms.application.command.PostApplicationService;
import com.github.thundax.bacon.upms.interfaces.dto.PostCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.PostPageRequest;
import com.github.thundax.bacon.upms.interfaces.dto.PostUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.TenantScopedRequest;
import com.github.thundax.bacon.upms.interfaces.response.PostPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/posts")
@Tag(name = "UPMS-Post", description = "岗位管理接口")
public class PostController {

    private final PostApplicationService postApplicationService;
    private final TenantRequestResolver tenantRequestResolver;

    public PostController(PostApplicationService postApplicationService,
                          TenantRequestResolver tenantRequestResolver) {
        this.postApplicationService = postApplicationService;
        this.tenantRequestResolver = tenantRequestResolver;
    }

    @Operation(summary = "分页查询岗位")
    @HasPermission("sys:post:view")
    @SysLog(module = "UPMS", action = "分页查询岗位", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public PostPageResponse pagePosts(@Valid @ModelAttribute PostPageRequest request) {
        return PostPageResponse.from(postApplicationService.pagePosts(new PostPageQueryDTO(
                tenantRequestResolver.resolveTenantId(request.getTenantNo()),
                request.getCode(), request.getName(), request.getDepartmentId(),
                request.getStatus() == null ? null : request.getStatus().name(),
                request.getPageNo(), request.getPageSize())));
    }

    @Operation(summary = "按岗位 ID 查询岗位")
    @HasPermission("sys:post:view")
    @SysLog(module = "UPMS", action = "查询岗位详情", eventType = LogEventType.QUERY)
    @GetMapping("/{postId}")
    public PostResponse getPostById(@PathVariable Long postId, @ModelAttribute TenantScopedRequest request) {
        return PostResponse.from(postApplicationService.getPostById(
                tenantRequestResolver.resolveTenantId(request.getTenantNo()), postId));
    }

    @Operation(summary = "创建岗位")
    @HasPermission("sys:post:create")
    @SysLog(module = "UPMS", action = "创建岗位", eventType = LogEventType.CREATE)
    @PostMapping
    public PostResponse createPost(@RequestBody PostCreateRequest request) {
        return PostResponse.from(postApplicationService.createPost(
                tenantRequestResolver.resolveTenantId(request.tenantNo()), request.code(), request.name(),
                request.departmentId()));
    }

    @Operation(summary = "修改岗位")
    @HasPermission("sys:post:update")
    @SysLog(module = "UPMS", action = "修改岗位", eventType = LogEventType.UPDATE)
    @PutMapping("/{postId}")
    public PostResponse updatePost(@PathVariable Long postId, @RequestBody PostUpdateRequest request) {
        return PostResponse.from(postApplicationService.updatePost(
                tenantRequestResolver.resolveTenantId(request.tenantNo()), postId, request.code(),
                request.name(), request.departmentId(), request.status()));
    }

    @Operation(summary = "删除岗位")
    @HasPermission("sys:post:delete")
    @SysLog(module = "UPMS", action = "删除岗位", eventType = LogEventType.DELETE)
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId, @ModelAttribute TenantScopedRequest request) {
        postApplicationService.deletePost(tenantRequestResolver.resolveTenantId(request.getTenantNo()), postId);
    }
}
