package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.command.ResourceCommandApplicationService;
import com.github.thundax.bacon.upms.application.query.ResourceQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.assembler.ResourceInterfaceAssembler;
import com.github.thundax.bacon.upms.interfaces.request.ResourceCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.ResourcePageRequest;
import com.github.thundax.bacon.upms.interfaces.request.ResourceUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.ResourcePageResponse;
import com.github.thundax.bacon.upms.interfaces.response.ResourceResponse;
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
@RequestMapping("/upms/resources")
@Validated
@Tag(name = "UPMS-Resource", description = "资源权限管理接口")
public class ResourceController {

    private final ResourceCommandApplicationService resourceCommandApplicationService;
    private final ResourceQueryApplicationService resourceQueryApplicationService;

    public ResourceController(
            ResourceCommandApplicationService resourceCommandApplicationService,
            ResourceQueryApplicationService resourceQueryApplicationService) {
        this.resourceCommandApplicationService = resourceCommandApplicationService;
        this.resourceQueryApplicationService = resourceQueryApplicationService;
    }

    @Operation(summary = "分页查询资源")
    @HasPermission("sys:resource:view")
    @SysLog(module = "UPMS", action = "分页查询资源", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public ResourcePageResponse page(@Valid @ModelAttribute ResourcePageRequest request) {
        return ResourceInterfaceAssembler.toPageResponse(
                resourceQueryApplicationService.page(ResourceInterfaceAssembler.toPageQuery(request)));
    }

    @Operation(summary = "按资源 ID 查询资源")
    @HasPermission("sys:resource:view")
    @SysLog(module = "UPMS", action = "查询资源详情", eventType = LogEventType.QUERY)
    @GetMapping("/{resourceId}")
    public ResourceResponse getResourceById(
            @PathVariable("resourceId") @Positive(message = "resourceId must be greater than 0") Long resourceId) {
        return ResourceInterfaceAssembler.toResponse(
                resourceQueryApplicationService.getById(ResourceInterfaceAssembler.toResourceId(resourceId)));
    }

    @Operation(summary = "创建资源")
    @HasPermission("sys:resource:create")
    @SysLog(module = "UPMS", action = "创建资源", eventType = LogEventType.CREATE)
    @PostMapping
    public ResourceResponse createResource(@Valid @RequestBody ResourceCreateRequest request) {
        return ResourceInterfaceAssembler.toResponse(
                resourceCommandApplicationService.create(ResourceInterfaceAssembler.toCreateCommand(request)));
    }

    @Operation(summary = "修改资源")
    @HasPermission("sys:resource:update")
    @SysLog(module = "UPMS", action = "修改资源", eventType = LogEventType.UPDATE)
    @PutMapping("/{resourceId}")
    public ResourceResponse updateResource(
            @PathVariable("resourceId") @Positive(message = "resourceId must be greater than 0") Long resourceId,
            @Valid @RequestBody ResourceUpdateRequest request) {
        return ResourceInterfaceAssembler.toResponse(
                resourceCommandApplicationService.update(ResourceInterfaceAssembler.toUpdateCommand(resourceId, request)));
    }

    @Operation(summary = "删除资源")
    @HasPermission("sys:resource:delete")
    @SysLog(module = "UPMS", action = "删除资源", eventType = LogEventType.DELETE)
    @DeleteMapping("/{resourceId}")
    public void deleteResource(
            @PathVariable("resourceId") @Positive(message = "resourceId must be greater than 0") Long resourceId) {
        resourceCommandApplicationService.delete(ResourceInterfaceAssembler.toResourceId(resourceId));
    }
}
