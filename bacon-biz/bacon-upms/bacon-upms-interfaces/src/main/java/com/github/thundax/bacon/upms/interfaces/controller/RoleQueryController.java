package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.service.RoleApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/roles")
@Tag(name = "UPMS-Role", description = "角色查询接口")
public class RoleQueryController {

    private final RoleApplicationService roleApplicationService;

    public RoleQueryController(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    @Operation(summary = "按角色 ID 查询角色")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询角色详情", eventType = LogEventType.QUERY)
    @GetMapping("/{roleId}")
    public RoleDTO getRoleById(@RequestParam("tenantId") Long tenantId, @PathVariable Long roleId) {
        return roleApplicationService.getRoleById(tenantId, roleId);
    }

    @Operation(summary = "查询用户角色列表")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询用户角色", eventType = LogEventType.QUERY)
    @GetMapping
    public List<RoleDTO> getRolesByUserId(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return roleApplicationService.getRolesByUserId(tenantId, userId);
    }
}
