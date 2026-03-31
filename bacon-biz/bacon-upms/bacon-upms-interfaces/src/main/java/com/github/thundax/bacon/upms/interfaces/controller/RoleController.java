package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.api.dto.RolePageQueryDTO;
import com.github.thundax.bacon.upms.application.command.RoleApplicationService;
import com.github.thundax.bacon.upms.interfaces.dto.RoleCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleDataScopeAssignRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleMenuAssignRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RolePageRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleResourceAssignRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.TenantScopedRequest;
import com.github.thundax.bacon.upms.interfaces.response.RoleDataScopeResponse;
import com.github.thundax.bacon.upms.interfaces.response.RolePageResponse;
import com.github.thundax.bacon.upms.interfaces.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
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
@RequestMapping("/upms/roles")
@Tag(name = "UPMS-Role", description = "角色管理接口")
public class RoleController {

    private final RoleApplicationService roleApplicationService;
    private final TenantRequestResolver tenantRequestResolver;

    public RoleController(RoleApplicationService roleApplicationService,
                          TenantRequestResolver tenantRequestResolver) {
        this.roleApplicationService = roleApplicationService;
        this.tenantRequestResolver = tenantRequestResolver;
    }

    @Operation(summary = "分页查询角色")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "分页查询角色", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public RolePageResponse pageRoles(@Valid @ModelAttribute RolePageRequest request) {
        return RolePageResponse.from(roleApplicationService.pageRoles(new RolePageQueryDTO(
                tenantRequestResolver.resolveTenantId(request.getTenantNo()),
                request.getCode(), request.getName(),
                request.getRoleType() == null ? null : request.getRoleType().name(),
                request.getStatus() == null ? null : request.getStatus().name(), request.getPageNo(),
                request.getPageSize())));
    }

    @Operation(summary = "创建角色")
    @HasPermission("sys:role:create")
    @SysLog(module = "UPMS", action = "创建角色", eventType = LogEventType.CREATE)
    @PostMapping
    public RoleResponse createRole(@RequestBody RoleCreateRequest request) {
        return RoleResponse.from(roleApplicationService.createRole(
                tenantRequestResolver.resolveTenantId(request.tenantNo()), request.code(), request.name(),
                request.roleType(), request.dataScopeType()));
    }

    @Operation(summary = "修改角色")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "修改角色", eventType = LogEventType.UPDATE)
    @PutMapping("/{roleId}")
    public RoleResponse updateRole(@PathVariable Long roleId, @RequestBody RoleUpdateRequest request) {
        return RoleResponse.from(roleApplicationService.updateRole(
                tenantRequestResolver.resolveTenantId(request.tenantNo()), roleId, request.code(),
                request.name(), request.roleType(), request.dataScopeType()));
    }

    @Operation(summary = "按角色 ID 查询角色")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色详情", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}")
    public RoleResponse getRoleById(@PathVariable Long roleId, @ModelAttribute TenantScopedRequest request) {
        return RoleResponse.from(roleApplicationService.getRoleById(request.getTenantNo(), roleId));
    }

    @Operation(summary = "启用或停用角色")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "变更角色状态", eventType = LogEventType.UPDATE)
    @PutMapping("/{roleId}/status")
    public RoleResponse updateRoleStatus(@PathVariable Long roleId, @RequestBody RoleStatusUpdateRequest request) {
        return RoleResponse.from(roleApplicationService.updateRoleStatus(
                tenantRequestResolver.resolveTenantId(request.tenantNo()), roleId, request.status()));
    }

    @Operation(summary = "删除角色")
    @HasPermission("sys:role:delete")
    @SysLog(module = "UPMS", action = "删除角色", eventType = LogEventType.DELETE)
    @DeleteMapping("/{roleId}")
    public void deleteRole(@PathVariable Long roleId, @ModelAttribute TenantScopedRequest request) {
        roleApplicationService.deleteRole(tenantRequestResolver.resolveTenantId(request.getTenantNo()), roleId);
    }

    @Operation(summary = "分配角色菜单")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "分配角色菜单", eventType = LogEventType.GRANT)
    @GetMapping("/{roleId}/menus")
    public Set<Long> getAssignedMenus(@PathVariable Long roleId, @ModelAttribute TenantScopedRequest request) {
        return roleApplicationService.getAssignedMenus(tenantRequestResolver.resolveTenantId(request.getTenantNo()), roleId);
    }

    @Operation(summary = "分配角色菜单")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "分配角色菜单", eventType = LogEventType.GRANT)
    @PutMapping("/{roleId}/menus")
    public Set<Long> assignMenus(@PathVariable Long roleId, @RequestBody RoleMenuAssignRequest request) {
        return roleApplicationService.assignMenus(tenantRequestResolver.resolveTenantId(request.tenantNo()), roleId,
                request.menuIds());
    }

    @Operation(summary = "查询角色资源授权")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色资源授权", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}/resources")
    public Set<String> getAssignedResources(@PathVariable Long roleId, @ModelAttribute TenantScopedRequest request) {
        return roleApplicationService.getAssignedResources(
                tenantRequestResolver.resolveTenantId(request.getTenantNo()), roleId);
    }

    @Operation(summary = "分配角色资源")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "分配角色资源", eventType = LogEventType.GRANT)
    @PutMapping("/{roleId}/resources")
    public Set<String> assignResources(@PathVariable Long roleId, @RequestBody RoleResourceAssignRequest request) {
        return roleApplicationService.assignResources(
                tenantRequestResolver.resolveTenantId(request.tenantNo()), roleId, request.resourceCodes());
    }

    @Operation(summary = "查询角色数据权限配置")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色数据权限配置", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}/data-scope")
    public RoleDataScopeResponse getAssignedDataScope(@PathVariable Long roleId,
                                                      @ModelAttribute TenantScopedRequest request) {
        Long tenantId = tenantRequestResolver.resolveTenantId(request.getTenantNo());
        return new RoleDataScopeResponse(
                roleApplicationService.getAssignedDataScopeType(tenantId, roleId),
                roleApplicationService.getAssignedDataScopeDepartments(tenantId, roleId)
        );
    }

    @Operation(summary = "配置角色数据权限")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "配置角色数据权限", eventType = LogEventType.GRANT)
    @PutMapping("/{roleId}/data-scope")
    public Set<Long> assignDataScope(@PathVariable Long roleId, @RequestBody RoleDataScopeAssignRequest request) {
        return roleApplicationService.assignDataScope(
                tenantRequestResolver.resolveTenantId(request.tenantNo()), roleId, request.dataScopeType(),
                request.departmentIds());
    }
}
