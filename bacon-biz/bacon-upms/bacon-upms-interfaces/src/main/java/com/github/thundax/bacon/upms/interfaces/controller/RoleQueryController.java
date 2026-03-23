package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.service.RoleApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/upms/roles")
public class RoleQueryController {

    private final RoleApplicationService roleApplicationService;

    public RoleQueryController(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色详情", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}")
    public RoleDTO getRoleById(@RequestParam("tenantId") Long tenantId, @PathVariable Long roleId) {
        return roleApplicationService.getRoleById(tenantId, roleId);
    }

    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询用户角色", eventType = LogEventType.QUERY)
    @GetMapping
    public List<RoleDTO> getRolesByUserId(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return roleApplicationService.getRolesByUserId(tenantId, userId);
    }
}
