package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.dto.TenantScopedRequest;
import com.github.thundax.bacon.upms.interfaces.response.UserDataScopeResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserMenuTreeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/users/{userId}")
@Tag(name = "UPMS-Permission", description = "权限管理接口")
public class PermissionController {

    private final PermissionQueryApplicationService permissionQueryService;

    public PermissionController(PermissionQueryApplicationService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }

    @Operation(summary = "查询用户菜单树")
    @HasPermission("sys:permission:view")
    @SysLog(module = "UPMS", action = "查询用户菜单树", eventType = LogEventType.QUERY)
    @GetMapping("/menu-tree")
    public List<UserMenuTreeResponse> getUserMenuTree(@PathVariable String userId,
                                                      @ModelAttribute TenantScopedRequest request) {
        return permissionQueryService.getUserMenuTree(request.getTenantId(), userId).stream()
                .map(UserMenuTreeResponse::from)
                .toList();
    }

    @Operation(summary = "查询用户权限码")
    @HasPermission("sys:permission:view")
    @SysLog(module = "UPMS", action = "查询用户权限码", eventType = LogEventType.QUERY)
    @GetMapping("/permission-codes")
    public Set<String> getUserPermissionCodes(@PathVariable String userId,
                                              @ModelAttribute TenantScopedRequest request) {
        return permissionQueryService.getUserPermissionCodes(request.getTenantId(), userId);
    }

    @Operation(summary = "查询用户数据权限范围")
    @HasPermission("sys:permission:view")
    @SysLog(module = "UPMS", action = "查询用户数据范围", eventType = LogEventType.QUERY)
    @GetMapping("/data-scope")
    public UserDataScopeResponse getUserDataScope(@PathVariable String userId,
                                                  @ModelAttribute TenantScopedRequest request) {
        return UserDataScopeResponse.from(permissionQueryService.getUserDataScope(request.getTenantId(), userId));
    }
}
