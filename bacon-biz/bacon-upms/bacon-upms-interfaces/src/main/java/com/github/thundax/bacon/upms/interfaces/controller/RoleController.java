package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.command.RoleCommandApplicationService;
import com.github.thundax.bacon.upms.application.query.RoleQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.assembler.RoleInterfaceAssembler;
import com.github.thundax.bacon.upms.interfaces.request.RoleCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleDataScopeAssignRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleMenuAssignRequest;
import com.github.thundax.bacon.upms.interfaces.request.RolePageRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleResourceAssignRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.RoleUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.RoleDataScopeResponse;
import com.github.thundax.bacon.upms.interfaces.response.RoleDepartmentIdsResponse;
import com.github.thundax.bacon.upms.interfaces.response.RoleMenuIdsResponse;
import com.github.thundax.bacon.upms.interfaces.response.RolePageResponse;
import com.github.thundax.bacon.upms.interfaces.response.RoleResponse;
import com.github.thundax.bacon.upms.interfaces.response.RoleResourceCodesResponse;
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
@RequestMapping("/upms/roles")
@Validated
@Tag(name = "UPMS-Role", description = "角色管理接口")
public class RoleController {

    private final RoleCommandApplicationService roleCommandApplicationService;
    private final RoleQueryApplicationService roleQueryApplicationService;

    public RoleController(
            RoleCommandApplicationService roleCommandApplicationService,
            RoleQueryApplicationService roleQueryApplicationService) {
        this.roleCommandApplicationService = roleCommandApplicationService;
        this.roleQueryApplicationService = roleQueryApplicationService;
    }

    @Operation(summary = "分页查询角色")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "分页查询角色", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public RolePageResponse page(@Valid @ModelAttribute RolePageRequest request) {
        return RoleInterfaceAssembler.toPageResponse(
                roleQueryApplicationService.page(RoleInterfaceAssembler.toPageQuery(request)));
    }

    @Operation(summary = "创建角色")
    @HasPermission("sys:role:create")
    @SysLog(module = "UPMS", action = "创建角色", eventType = LogEventType.CREATE)
    @PostMapping
    public RoleResponse createRole(@Valid @RequestBody RoleCreateRequest request) {
        return RoleInterfaceAssembler.toResponse(
                roleCommandApplicationService.create(RoleInterfaceAssembler.toCreateCommand(request)));
    }

    @Operation(summary = "修改角色")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "修改角色", eventType = LogEventType.UPDATE)
    @PutMapping("/{roleId}")
    public RoleResponse updateRole(
            @PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId,
            @Valid @RequestBody RoleUpdateRequest request) {
        return RoleInterfaceAssembler.toResponse(
                roleCommandApplicationService.update(RoleInterfaceAssembler.toUpdateCommand(roleId, request)));
    }

    @Operation(summary = "按角色 ID 查询角色")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色详情", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}")
    public RoleResponse getRoleById(
            @PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId) {
        return RoleInterfaceAssembler.toResponse(
                roleQueryApplicationService.getById(RoleInterfaceAssembler.toRoleId(roleId)));
    }

    @Operation(summary = "启用或停用角色")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "变更角色状态", eventType = LogEventType.UPDATE)
    @PutMapping("/{roleId}/status")
    public RoleResponse updateRoleStatus(
            @PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId,
            @Valid @RequestBody RoleStatusUpdateRequest request) {
        return RoleInterfaceAssembler.toResponse(
                roleCommandApplicationService.updateStatus(
                        RoleInterfaceAssembler.toStatusUpdateCommand(roleId, request)));
    }

    @Operation(summary = "删除角色")
    @HasPermission("sys:role:delete")
    @SysLog(module = "UPMS", action = "删除角色", eventType = LogEventType.DELETE)
    @DeleteMapping("/{roleId}")
    public void delete(@PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId) {
        roleCommandApplicationService.delete(RoleInterfaceAssembler.toRoleId(roleId));
    }

    @Operation(summary = "分配角色菜单")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "分配角色菜单", eventType = LogEventType.GRANT)
    @GetMapping("/{roleId}/menus")
    public RoleMenuIdsResponse getAssignedMenus(
            @PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId) {
        return RoleInterfaceAssembler.toMenuIdsResponse(
                roleQueryApplicationService.getMenuIds(RoleInterfaceAssembler.toRoleId(roleId)));
    }

    @Operation(summary = "分配角色菜单")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "分配角色菜单", eventType = LogEventType.GRANT)
    @PutMapping("/{roleId}/menus")
    public RoleMenuIdsResponse assignMenus(
            @PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId,
            @Valid @RequestBody RoleMenuAssignRequest request) {
        return RoleInterfaceAssembler.toMenuIdsResponse(
                roleCommandApplicationService.updateMenuIds(
                        RoleInterfaceAssembler.toMenuAssignCommand(roleId, request)));
    }

    @Operation(summary = "查询角色资源授权")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色资源授权", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}/resources")
    public RoleResourceCodesResponse getAssignedResources(
            @PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId) {
        return RoleInterfaceAssembler.toResourceCodesResponse(
                roleQueryApplicationService.getResourceCodes(RoleInterfaceAssembler.toRoleId(roleId)));
    }

    @Operation(summary = "分配角色资源")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "分配角色资源", eventType = LogEventType.GRANT)
    @PutMapping("/{roleId}/resources")
    public RoleResourceCodesResponse assignResources(
            @PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId,
            @Valid @RequestBody RoleResourceAssignRequest request) {
        return RoleInterfaceAssembler.toResourceCodesResponse(
                roleCommandApplicationService.updateResourceCodes(
                        RoleInterfaceAssembler.toResourceAssignCommand(roleId, request)));
    }

    @Operation(summary = "查询角色数据权限配置")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色数据权限配置", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}/data-scope")
    public RoleDataScopeResponse getAssignedDataScope(
            @PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId) {
        return RoleInterfaceAssembler.toDataScopeResponse(
                roleQueryApplicationService.getDataScopeType(RoleInterfaceAssembler.toRoleId(roleId)).value(),
                roleQueryApplicationService.getDataScopeDepartmentIds(RoleInterfaceAssembler.toRoleId(roleId)));
    }

    @Operation(summary = "配置角色数据权限")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "配置角色数据权限", eventType = LogEventType.GRANT)
    @PutMapping("/{roleId}/data-scope")
    public RoleDepartmentIdsResponse assignDataScope(
            @PathVariable("roleId") @Positive(message = "roleId must be greater than 0") Long roleId,
            @Valid @RequestBody RoleDataScopeAssignRequest request) {
        return RoleInterfaceAssembler.toDepartmentIdsResponse(
                roleCommandApplicationService.updateDataScope(
                        RoleInterfaceAssembler.toDataScopeAssignCommand(roleId, request)));
    }
}
