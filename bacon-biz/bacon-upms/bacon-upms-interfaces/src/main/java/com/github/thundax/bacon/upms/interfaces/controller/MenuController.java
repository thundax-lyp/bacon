package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.command.MenuCommandApplicationService;
import com.github.thundax.bacon.upms.application.query.MenuQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.assembler.MenuInterfaceAssembler;
import com.github.thundax.bacon.upms.interfaces.request.MenuCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.MenuSortUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.MenuUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.MenuTreeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@WrappedApiController
@RequestMapping("/upms/menus")
@Validated
@Tag(name = "UPMS-Menu", description = "菜单管理接口")
public class MenuController {

    private final MenuCommandApplicationService menuCommandApplicationService;
    private final MenuQueryApplicationService menuQueryApplicationService;

    public MenuController(
            MenuCommandApplicationService menuCommandApplicationService,
            MenuQueryApplicationService menuQueryApplicationService) {
        this.menuCommandApplicationService = menuCommandApplicationService;
        this.menuQueryApplicationService = menuQueryApplicationService;
    }

    @Operation(summary = "查询菜单树")
    @HasPermission("sys:menu:view")
    @SysLog(module = "UPMS", action = "查询菜单树", eventType = LogEventType.QUERY)
    @GetMapping("/tree")
    public List<MenuTreeResponse> getMenuTree() {
        return MenuInterfaceAssembler.toTreeResponseList(menuQueryApplicationService.tree());
    }

    @Operation(summary = "创建菜单")
    @HasPermission("sys:menu:create")
    @SysLog(module = "UPMS", action = "创建菜单", eventType = LogEventType.CREATE)
    @PostMapping
    public MenuTreeResponse createMenu(@Valid @RequestBody MenuCreateRequest request) {
        return MenuInterfaceAssembler.toResponse(
                menuCommandApplicationService.create(MenuInterfaceAssembler.toCreateCommand(request)));
    }

    @Operation(summary = "修改菜单")
    @HasPermission("sys:menu:update")
    @SysLog(module = "UPMS", action = "修改菜单", eventType = LogEventType.UPDATE)
    @PutMapping("/{menuId}")
    public MenuTreeResponse updateMenu(
            @PathVariable("menuId") @Positive(message = "menuId must be greater than 0") Long menuId,
            @Valid @RequestBody MenuUpdateRequest request) {
        return MenuInterfaceAssembler.toResponse(
                menuCommandApplicationService.update(MenuInterfaceAssembler.toUpdateCommand(menuId, request)));
    }

    @Operation(summary = "删除菜单")
    @HasPermission("sys:menu:delete")
    @SysLog(module = "UPMS", action = "删除菜单", eventType = LogEventType.DELETE)
    @DeleteMapping("/{menuId}")
    public void delete(@PathVariable("menuId") @Positive(message = "menuId must be greater than 0") Long menuId) {
        menuCommandApplicationService.delete(MenuInterfaceAssembler.toMenuId(menuId));
    }

    @Operation(summary = "调整菜单排序")
    @HasPermission("sys:menu:update")
    @SysLog(module = "UPMS", action = "调整菜单排序", eventType = LogEventType.UPDATE)
    @PutMapping("/{menuId}/sort")
    public MenuTreeResponse updateSort(
            @PathVariable("menuId") @Positive(message = "menuId must be greater than 0") Long menuId,
            @Valid @RequestBody MenuSortUpdateRequest request) {
        return MenuInterfaceAssembler.toResponse(
                menuCommandApplicationService.updateSort(MenuInterfaceAssembler.toSortUpdateCommand(menuId, request)));
    }
}
