package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.application.service.PermissionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/upms/permissions")
public class PermissionQueryController {

    private final PermissionQueryService permissionQueryService;

    public PermissionQueryController(PermissionQueryService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }

    @HasPermission("sys:permission:view")
    @GetMapping("/menus")
    public List<UserMenuTreeDTO> getUserMenuTree(@RequestParam("tenantId") Long tenantId,
                                                 @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserMenuTree(tenantId, userId);
    }

    @HasPermission("sys:permission:view")
    @GetMapping("/codes")
    public Set<String> getUserPermissionCodes(@RequestParam("tenantId") Long tenantId,
                                              @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserPermissionCodes(tenantId, userId);
    }

    @HasPermission("sys:permission:view")
    @GetMapping("/data-scope")
    public UserDataScopeDTO getUserDataScope(@RequestParam("tenantId") Long tenantId,
                                             @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserDataScope(tenantId, userId);
    }
}
