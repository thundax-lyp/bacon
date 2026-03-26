package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.api.dto.TenantPageQueryDTO;
import com.github.thundax.bacon.upms.application.service.TenantApplicationService;
import com.github.thundax.bacon.upms.interfaces.dto.TenantCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.TenantPageRequest;
import com.github.thundax.bacon.upms.interfaces.dto.TenantStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.TenantUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.TenantPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.TenantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/upms/tenants")
@Tag(name = "UPMS-Tenant", description = "租户管理接口")
public class TenantController {

    private final TenantApplicationService tenantApplicationService;

    public TenantController(TenantApplicationService tenantApplicationService) {
        this.tenantApplicationService = tenantApplicationService;
    }

    @Operation(summary = "分页查询租户")
    @HasPermission("sys:tenant:view")
    @SysLog(module = "UPMS", action = "分页查询租户", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public TenantPageResponse pageTenants(@Valid @ModelAttribute TenantPageRequest request) {
        return TenantPageResponse.from(tenantApplicationService.pageTenants(new TenantPageQueryDTO(request.getTenantId(),
                request.getCode(), request.getName(), request.getStatus(), request.getPageNo(), request.getPageSize())));
    }

    @Operation(summary = "创建租户")
    @HasPermission("sys:tenant:create")
    @SysLog(module = "UPMS", action = "创建租户", eventType = LogEventType.CREATE)
    @PostMapping
    public TenantResponse createTenant(@RequestBody TenantCreateRequest request) {
        return TenantResponse.from(tenantApplicationService.createTenant(request.code(), request.name()));
    }

    @Operation(summary = "修改租户")
    @HasPermission("sys:tenant:update")
    @SysLog(module = "UPMS", action = "修改租户", eventType = LogEventType.UPDATE)
    @PutMapping("/{tenantId}")
    public TenantResponse updateTenant(@PathVariable Long tenantId, @RequestBody TenantUpdateRequest request) {
        return TenantResponse.from(tenantApplicationService.updateTenant(tenantId, request.code(), request.name()));
    }

    @Operation(summary = "按租户 ID 查询租户")
    @HasPermission("sys:tenant:view")
    @SysLog(module = "UPMS", action = "查询租户详情", eventType = LogEventType.QUERY)
    @GetMapping("/{tenantId}")
    public TenantResponse getTenantByTenantId(@PathVariable Long tenantId) {
        return TenantResponse.from(tenantApplicationService.getTenantByTenantId(tenantId));
    }

    @Operation(summary = "变更租户状态")
    @HasPermission("sys:tenant:update")
    @SysLog(module = "UPMS", action = "变更租户状态", eventType = LogEventType.UPDATE)
    @PutMapping("/{tenantId}/status")
    public TenantResponse updateTenantStatus(@PathVariable Long tenantId, @RequestBody TenantStatusUpdateRequest request) {
        return TenantResponse.from(tenantApplicationService.updateTenantStatus(tenantId, request.status()));
    }
}
