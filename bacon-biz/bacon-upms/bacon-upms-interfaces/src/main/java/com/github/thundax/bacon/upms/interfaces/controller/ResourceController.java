package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.CurrentTenant;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.api.dto.ResourcePageQueryDTO;
import com.github.thundax.bacon.upms.application.command.ResourceApplicationService;
import com.github.thundax.bacon.upms.interfaces.dto.ResourceCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.ResourcePageRequest;
import com.github.thundax.bacon.upms.interfaces.dto.ResourceUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.ResourcePageResponse;
import com.github.thundax.bacon.upms.interfaces.response.ResourceResponse;
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
@RequestMapping("/upms/resources")
@Tag(name = "UPMS-Resource", description = "资源权限管理接口")
public class ResourceController {

    private final ResourceApplicationService resourceApplicationService;

    public ResourceController(ResourceApplicationService resourceApplicationService) {
        this.resourceApplicationService = resourceApplicationService;
    }

    @Operation(summary = "分页查询资源")
    @HasPermission("sys:resource:view")
    @SysLog(module = "UPMS", action = "分页查询资源", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public ResourcePageResponse pageResources(
            @CurrentTenant Long tenantId, @Valid @ModelAttribute ResourcePageRequest request) {
        return ResourcePageResponse.from(resourceApplicationService.pageResources(new ResourcePageQueryDTO(
                request.getCode(),
                request.getName(),
                request.getResourceType() == null
                        ? null
                        : request.getResourceType().name(),
                request.getStatus() == null ? null : request.getStatus().name(),
                request.getPageNo(),
                request.getPageSize())));
    }

    @Operation(summary = "按资源 ID 查询资源")
    @HasPermission("sys:resource:view")
    @SysLog(module = "UPMS", action = "查询资源详情", eventType = LogEventType.QUERY)
    @GetMapping("/{resourceId}")
    public ResourceResponse getResourceById(@CurrentTenant Long tenantId, @PathVariable String resourceId) {
        return ResourceResponse.from(resourceApplicationService.getResourceById(TenantId.of(tenantId), resourceId));
    }

    @Operation(summary = "创建资源")
    @HasPermission("sys:resource:create")
    @SysLog(module = "UPMS", action = "创建资源", eventType = LogEventType.CREATE)
    @PostMapping
    public ResourceResponse createResource(@CurrentTenant Long tenantId, @RequestBody ResourceCreateRequest request) {
        return ResourceResponse.from(resourceApplicationService.createResource(
                TenantId.of(tenantId),
                request.code(),
                request.name(),
                request.resourceType(),
                request.httpMethod(),
                request.uri()));
    }

    @Operation(summary = "修改资源")
    @HasPermission("sys:resource:update")
    @SysLog(module = "UPMS", action = "修改资源", eventType = LogEventType.UPDATE)
    @PutMapping("/{resourceId}")
    public ResourceResponse updateResource(
            @CurrentTenant Long tenantId, @PathVariable String resourceId, @RequestBody ResourceUpdateRequest request) {
        return ResourceResponse.from(resourceApplicationService.updateResource(
                TenantId.of(tenantId),
                resourceId,
                request.code(),
                request.name(),
                request.resourceType(),
                request.httpMethod(),
                request.uri(),
                request.status()));
    }

    @Operation(summary = "删除资源")
    @HasPermission("sys:resource:delete")
    @SysLog(module = "UPMS", action = "删除资源", eventType = LogEventType.DELETE)
    @DeleteMapping("/{resourceId}")
    public void deleteResource(@CurrentTenant Long tenantId, @PathVariable String resourceId) {
        resourceApplicationService.deleteResource(TenantId.of(tenantId), resourceId);
    }
}
