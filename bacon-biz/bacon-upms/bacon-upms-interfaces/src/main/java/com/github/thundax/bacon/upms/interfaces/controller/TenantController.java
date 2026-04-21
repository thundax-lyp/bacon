package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.command.TenantCommandApplicationService;
import com.github.thundax.bacon.upms.application.query.TenantQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.assembler.TenantInterfaceAssembler;
import com.github.thundax.bacon.upms.interfaces.request.TenantCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.TenantPageRequest;
import com.github.thundax.bacon.upms.interfaces.request.TenantStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.TenantUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.TenantPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.TenantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
@RequestMapping("/upms/tenants")
@Validated
@Tag(name = "UPMS-Tenant", description = "租户管理接口")
public class TenantController {

    private final TenantCommandApplicationService tenantCommandApplicationService;
    private final TenantQueryApplicationService tenantQueryApplicationService;

    public TenantController(
            TenantCommandApplicationService tenantCommandApplicationService,
            TenantQueryApplicationService tenantQueryApplicationService) {
        this.tenantCommandApplicationService = tenantCommandApplicationService;
        this.tenantQueryApplicationService = tenantQueryApplicationService;
    }

    @Operation(summary = "分页查询租户")
    @HasPermission("sys:tenant:view")
    @SysLog(module = "UPMS", action = "分页查询租户", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public TenantPageResponse page(@Valid @ModelAttribute TenantPageRequest request) {
        return TenantInterfaceAssembler.toPageResponse(
                tenantQueryApplicationService.page(TenantInterfaceAssembler.toPageQuery(request)));
    }

    @Operation(summary = "创建租户")
    @HasPermission("sys:tenant:create")
    @SysLog(module = "UPMS", action = "创建租户", eventType = LogEventType.CREATE)
    @PostMapping
    public TenantResponse createTenant(@Valid @RequestBody TenantCreateRequest request) {
        return TenantInterfaceAssembler.toResponse(
                tenantCommandApplicationService.create(TenantInterfaceAssembler.toCreateCommand(request)));
    }

    @Operation(summary = "修改租户")
    @HasPermission("sys:tenant:update")
    @SysLog(module = "UPMS", action = "修改租户", eventType = LogEventType.UPDATE)
    @PutMapping("/{tenantId}")
    public TenantResponse updateTenant(
            @PathVariable("tenantId") @Positive(message = "tenantId must be greater than 0") Long tenantId,
            @Valid @RequestBody TenantUpdateRequest request) {
        return TenantInterfaceAssembler.toResponse(
                tenantCommandApplicationService.update(TenantInterfaceAssembler.toUpdateCommand(tenantId, request)));
    }

    @Operation(summary = "按租户编号查询租户")
    @HasPermission("sys:tenant:view")
    @SysLog(module = "UPMS", action = "查询租户详情", eventType = LogEventType.QUERY)
    @GetMapping("/{tenantId}")
    public TenantResponse getTenantByTenantId(
            @PathVariable("tenantId") @Positive(message = "tenantId must be greater than 0") Long tenantId) {
        return TenantInterfaceAssembler.toResponse(tenantQueryApplicationService.getById(TenantId.of(tenantId)));
    }

    @Operation(summary = "变更租户状态")
    @HasPermission("sys:tenant:update")
    @SysLog(module = "UPMS", action = "变更租户状态", eventType = LogEventType.UPDATE)
    @PutMapping("/{tenantId}/status")
    public TenantResponse updateTenantStatus(
            @PathVariable("tenantId") @Positive(message = "tenantId must be greater than 0") Long tenantId,
            @Valid @RequestBody TenantStatusUpdateRequest request) {
        return TenantInterfaceAssembler.toResponse(tenantCommandApplicationService.updateStatus(
                TenantInterfaceAssembler.toStatusUpdateCommand(tenantId, request)));
    }
}
