package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.service.MenuApplicationService;
import com.github.thundax.bacon.upms.interfaces.dto.MenuCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.MenuSortUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.MenuUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.TenantScopedRequest;
import com.github.thundax.bacon.upms.interfaces.response.MenuTreeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
@RequestMapping("/upms/menus")
@Tag(name = "UPMS-Menu", description = "菜单管理接口")
public class MenuController {

    private final MenuApplicationService menuApplicationService;

    public MenuController(MenuApplicationService menuApplicationService) {
        this.menuApplicationService = menuApplicationService;
    }

    @Operation(summary = "查询菜单树")
    @HasPermission("sys:menu:view")
    @SysLog(module = "UPMS", action = "查询菜单树", eventType = LogEventType.QUERY)
    @GetMapping("/tree")
    public List<MenuTreeResponse> getMenuTree(@ModelAttribute TenantScopedRequest request) {
        return menuApplicationService.getMenuTree(request.getTenantId()).stream().map(MenuTreeResponse::from).toList();
    }

    @Operation(summary = "创建菜单")
    @HasPermission("sys:menu:create")
    @SysLog(module = "UPMS", action = "创建菜单", eventType = LogEventType.CREATE)
    @PostMapping
    public MenuTreeResponse createMenu(@RequestBody MenuCreateRequest request) {
        return MenuTreeResponse.from(menuApplicationService.createMenu(request.tenantId(), request.menuType(), request.name(),
                request.parentId(), request.routePath(), request.componentName(), request.icon(), request.sort(),
                request.permissionCode()));
    }

    @Operation(summary = "修改菜单")
    @HasPermission("sys:menu:update")
    @SysLog(module = "UPMS", action = "修改菜单", eventType = LogEventType.UPDATE)
    @PutMapping("/{menuId}")
    public MenuTreeResponse updateMenu(@PathVariable Long menuId, @RequestBody MenuUpdateRequest request) {
        return MenuTreeResponse.from(menuApplicationService.updateMenu(request.tenantId(), menuId, request.menuType(),
                request.name(), request.parentId(), request.routePath(), request.componentName(), request.icon(),
                request.sort(), request.permissionCode()));
    }

    @Operation(summary = "删除菜单")
    @HasPermission("sys:menu:delete")
    @SysLog(module = "UPMS", action = "删除菜单", eventType = LogEventType.DELETE)
    @DeleteMapping("/{menuId}")
    public void deleteMenu(@PathVariable Long menuId, @ModelAttribute TenantScopedRequest request) {
        menuApplicationService.deleteMenu(request.getTenantId(), menuId);
    }

    @Operation(summary = "调整菜单排序")
    @HasPermission("sys:menu:update")
    @SysLog(module = "UPMS", action = "调整菜单排序", eventType = LogEventType.UPDATE)
    @PutMapping("/{menuId}/sort")
    public MenuTreeResponse updateSort(@PathVariable Long menuId, @RequestBody MenuSortUpdateRequest request) {
        return MenuTreeResponse.from(menuApplicationService.updateMenuSort(request.tenantId(), menuId, request.sort()));
    }
}
