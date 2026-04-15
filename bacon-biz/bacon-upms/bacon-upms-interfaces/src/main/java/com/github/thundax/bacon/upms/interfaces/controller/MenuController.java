package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.command.MenuApplicationService;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.interfaces.dto.MenuCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.MenuSortUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.MenuUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.MenuTreeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<MenuTreeResponse> getMenuTree() {
        return menuApplicationService.getMenuTree().stream()
                .map(MenuTreeResponse::from)
                .toList();
    }

    @Operation(summary = "创建菜单")
    @HasPermission("sys:menu:create")
    @SysLog(module = "UPMS", action = "创建菜单", eventType = LogEventType.CREATE)
    @PostMapping
    public MenuTreeResponse createMenu(@Valid @RequestBody MenuCreateRequest request) {
        return MenuTreeResponse.from(menuApplicationService.createMenu(
                MenuType.from(trimPreservingNull(request.menuType())),
                trimPreservingNull(request.name()),
                MenuIdCodec.toDomain(request.parentId()),
                trimPreservingNull(request.routePath()),
                trimPreservingNull(request.componentName()),
                trimPreservingNull(request.icon()),
                request.sort(),
                trimPreservingNull(request.permissionCode())));
    }

    @Operation(summary = "修改菜单")
    @HasPermission("sys:menu:update")
    @SysLog(module = "UPMS", action = "修改菜单", eventType = LogEventType.UPDATE)
    @PutMapping("/{menuId}")
    public MenuTreeResponse updateMenu(
            @PathVariable("menuId") Long menuId, @Valid @RequestBody MenuUpdateRequest request) {
        return MenuTreeResponse.from(menuApplicationService.updateMenu(
                MenuIdCodec.toDomain(menuId),
                MenuType.from(trimPreservingNull(request.menuType())),
                trimPreservingNull(request.name()),
                MenuIdCodec.toDomain(request.parentId()),
                trimPreservingNull(request.routePath()),
                trimPreservingNull(request.componentName()),
                trimPreservingNull(request.icon()),
                request.sort(),
                trimPreservingNull(request.permissionCode())));
    }

    @Operation(summary = "删除菜单")
    @HasPermission("sys:menu:delete")
    @SysLog(module = "UPMS", action = "删除菜单", eventType = LogEventType.DELETE)
    @DeleteMapping("/{menuId}")
    public void deleteMenu(@PathVariable("menuId") Long menuId) {
        menuApplicationService.deleteMenu(MenuIdCodec.toDomain(menuId));
    }

    @Operation(summary = "调整菜单排序")
    @HasPermission("sys:menu:update")
    @SysLog(module = "UPMS", action = "调整菜单排序", eventType = LogEventType.UPDATE)
    @PutMapping("/{menuId}/sort")
    public MenuTreeResponse updateSort(
            @PathVariable("menuId") Long menuId, @RequestBody MenuSortUpdateRequest request) {
        return MenuTreeResponse.from(
                menuApplicationService.updateMenuSort(MenuIdCodec.toDomain(menuId), request.sort()));
    }

    private String trimPreservingNull(String value) {
        return value == null ? null : value.trim();
    }
}
