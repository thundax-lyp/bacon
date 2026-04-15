package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.codec.RoleIdCodec;
import com.github.thundax.bacon.upms.application.command.RoleApplicationService;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.interfaces.dto.RoleCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleDataScopeAssignRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleMenuAssignRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RolePageRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleResourceAssignRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.RoleUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.RoleDataScopeResponse;
import com.github.thundax.bacon.upms.interfaces.response.RolePageResponse;
import com.github.thundax.bacon.upms.interfaces.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
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

    public RoleController(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    @Operation(summary = "分页查询角色")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "分页查询角色", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public RolePageResponse pageRoles(@Valid @ModelAttribute RolePageRequest request) {
        return RolePageResponse.from(roleApplicationService.pageRoles(
                request.getCode(),
                request.getName(),
                request.getRoleType() == null ? null : RoleType.from(request.getRoleType()),
                request.getStatus() == null ? null : RoleStatus.from(request.getStatus()),
                request.getPageNo(),
                request.getPageSize()));
    }

    @Operation(summary = "创建角色")
    @HasPermission("sys:role:create")
    @SysLog(module = "UPMS", action = "创建角色", eventType = LogEventType.CREATE)
    @PostMapping
    public RoleResponse createRole(@RequestBody RoleCreateRequest request) {
        return RoleResponse.from(roleApplicationService.createRole(
                request.code(),
                request.name(),
                request.roleType() == null ? null : RoleType.from(request.roleType()),
                request.dataScopeType() == null ? null : RoleDataScopeType.from(request.dataScopeType())));
    }

    @Operation(summary = "修改角色")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "修改角色", eventType = LogEventType.UPDATE)
    @PutMapping("/{roleId}")
    public RoleResponse updateRole(@PathVariable("roleId") Long roleId, @RequestBody RoleUpdateRequest request) {
        return RoleResponse.from(roleApplicationService.updateRole(
                RoleIdCodec.toDomain(roleId),
                request.code(),
                request.name(),
                request.roleType() == null ? null : RoleType.from(request.roleType()),
                request.dataScopeType() == null ? null : RoleDataScopeType.from(request.dataScopeType())));
    }

    @Operation(summary = "按角色 ID 查询角色")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色详情", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}")
    public RoleResponse getRoleById(@PathVariable("roleId") Long roleId) {
        return RoleResponse.from(roleApplicationService.getRoleById(RoleIdCodec.toDomain(roleId)));
    }

    @Operation(summary = "启用或停用角色")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "变更角色状态", eventType = LogEventType.UPDATE)
    @PutMapping("/{roleId}/status")
    public RoleResponse updateRoleStatus(
            @PathVariable("roleId") Long roleId, @RequestBody RoleStatusUpdateRequest request) {
        return RoleResponse.from(roleApplicationService.updateRoleStatus(
                RoleIdCodec.toDomain(roleId), request.status() == null ? null : RoleStatus.from(request.status())));
    }

    @Operation(summary = "删除角色")
    @HasPermission("sys:role:delete")
    @SysLog(module = "UPMS", action = "删除角色", eventType = LogEventType.DELETE)
    @DeleteMapping("/{roleId}")
    public void deleteRole(@PathVariable("roleId") Long roleId) {
        roleApplicationService.deleteRole(RoleIdCodec.toDomain(roleId));
    }

    @Operation(summary = "分配角色菜单")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "分配角色菜单", eventType = LogEventType.GRANT)
    @GetMapping("/{roleId}/menus")
    public Set<String> getAssignedMenus(@PathVariable("roleId") Long roleId) {
        return roleApplicationService.getAssignedMenus(RoleIdCodec.toDomain(roleId));
    }

    @Operation(summary = "分配角色菜单")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "分配角色菜单", eventType = LogEventType.GRANT)
    @PutMapping("/{roleId}/menus")
    public Set<String> assignMenus(@PathVariable("roleId") Long roleId, @RequestBody RoleMenuAssignRequest request) {
        return roleApplicationService.assignMenus(
                RoleIdCodec.toDomain(roleId),
                request.menuIds() == null
                        ? Set.of()
                        : request.menuIds().stream().map(MenuIdCodec::toDomain).collect(Collectors.toSet()));
    }

    @Operation(summary = "查询角色资源授权")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色资源授权", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}/resources")
    public Set<String> getAssignedResources(@PathVariable("roleId") Long roleId) {
        return roleApplicationService.getAssignedResources(RoleIdCodec.toDomain(roleId));
    }

    @Operation(summary = "分配角色资源")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "分配角色资源", eventType = LogEventType.GRANT)
    @PutMapping("/{roleId}/resources")
    public Set<String> assignResources(
            @PathVariable("roleId") Long roleId, @RequestBody RoleResourceAssignRequest request) {
        return roleApplicationService.assignResources(RoleIdCodec.toDomain(roleId), request.resourceCodes());
    }

    @Operation(summary = "查询角色数据权限配置")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色数据权限配置", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}/data-scope")
    public RoleDataScopeResponse getAssignedDataScope(@PathVariable("roleId") Long roleId) {
        return new RoleDataScopeResponse(
                roleApplicationService
                        .getAssignedDataScopeType(RoleIdCodec.toDomain(roleId))
                        .value(),
                roleApplicationService.getAssignedDataScopeDepartments(RoleIdCodec.toDomain(roleId)).stream()
                        .map(DepartmentId::value)
                        .collect(Collectors.toSet()));
    }

    @Operation(summary = "配置角色数据权限")
    @HasPermission("sys:role:update")
    @SysLog(module = "UPMS", action = "配置角色数据权限", eventType = LogEventType.GRANT)
    @PutMapping("/{roleId}/data-scope")
    public Set<Long> assignDataScope(
            @PathVariable("roleId") Long roleId, @RequestBody RoleDataScopeAssignRequest request) {
        return roleApplicationService
                .assignDataScope(
                        RoleIdCodec.toDomain(roleId),
                        request.dataScopeType() == null ? null : RoleDataScopeType.from(request.dataScopeType()),
                        request.departmentIds() == null
                                ? Set.of()
                                : request.departmentIds().stream()
                                        .map(DepartmentIdCodec::toDomain)
                                        .collect(Collectors.toSet()))
                .stream()
                .map(DepartmentId::value)
                .collect(Collectors.toSet());
    }
}
