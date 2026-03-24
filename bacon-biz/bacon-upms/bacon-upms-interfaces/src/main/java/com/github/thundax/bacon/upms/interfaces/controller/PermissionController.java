package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.service.PermissionQueryService;
import com.github.thundax.bacon.upms.interfaces.dto.UserPermissionQueryRequest;
import com.github.thundax.bacon.upms.interfaces.response.UserDataScopeResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserMenuTreeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/permissions")
@Tag(name = "UPMS-Permission", description = "权限管理接口")
public class PermissionController {

    private final PermissionQueryService permissionQueryService;

    public PermissionController(PermissionQueryService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }

    @Operation(summary = "查询用户菜单树")
    @HasPermission("sys:permission:view")
    @SysLog(module = "UPMS", action = "查询用户菜单树", eventType = LogEventType.QUERY)
    @GetMapping("/menus")
    public List<UserMenuTreeResponse> getUserMenuTree(@ModelAttribute UserPermissionQueryRequest request) {
        return permissionQueryService.getUserMenuTree(request.getTenantId(), request.getUserId()).stream()
                .map(UserMenuTreeResponse::from)
                .toList();
    }

    @Operation(summary = "查询用户权限码")
    @HasPermission("sys:permission:view")
    @SysLog(module = "UPMS", action = "查询用户权限码", eventType = LogEventType.QUERY)
    @GetMapping("/codes")
    public Set<String> getUserPermissionCodes(@ModelAttribute UserPermissionQueryRequest request) {
        return permissionQueryService.getUserPermissionCodes(request.getTenantId(), request.getUserId());
    }

    @Operation(summary = "查询用户数据权限范围")
    @HasPermission("sys:permission:view")
    @SysLog(module = "UPMS", action = "查询用户数据范围", eventType = LogEventType.QUERY)
    @GetMapping("/data-scope")
    public UserDataScopeResponse getUserDataScope(@ModelAttribute UserPermissionQueryRequest request) {
        return UserDataScopeResponse.from(permissionQueryService.getUserDataScope(request.getTenantId(),
                request.getUserId()));
    }
}
