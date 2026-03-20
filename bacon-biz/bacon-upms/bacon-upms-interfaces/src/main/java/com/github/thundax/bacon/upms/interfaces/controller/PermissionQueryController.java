package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.application.service.PermissionQueryService;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/permissions")
public class PermissionQueryController {

    private final PermissionQueryService permissionQueryService;

    public PermissionQueryController(PermissionQueryService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }

    @GetMapping("/menus")
    public List<UserMenuTreeDTO> getUserMenuTree(@RequestParam("tenantId") Long tenantId,
                                                 @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserMenuTree(tenantId, userId);
    }

    @GetMapping("/codes")
    public Set<String> getUserPermissionCodes(@RequestParam("tenantId") Long tenantId,
                                              @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserPermissionCodes(tenantId, userId);
    }

    @GetMapping("/data-scope")
    public UserDataScopeDTO getUserDataScope(@RequestParam("tenantId") Long tenantId,
                                             @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserDataScope(tenantId, userId);
    }
}
