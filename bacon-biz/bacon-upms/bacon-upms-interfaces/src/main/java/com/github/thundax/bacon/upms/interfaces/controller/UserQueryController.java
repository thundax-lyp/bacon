package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/users")
@Tag(name = "UPMS User", description = "用户查询接口")
public class UserQueryController {

    private final UserApplicationService userApplicationService;

    public UserQueryController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Operation(summary = "按用户 ID 查询用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询用户详情", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}")
    public UserDTO getUserById(@RequestParam("tenantId") Long tenantId, @PathVariable Long userId) {
        return userApplicationService.getUserById(tenantId, userId);
    }

    @Operation(summary = "按身份标识查询用户身份")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询用户身份", eventType = LogEventType.QUERY)
    @GetMapping("/identity")
    public UserIdentityDTO getUserIdentity(@RequestParam("tenantId") Long tenantId,
                                           @RequestParam("identityType") String identityType,
                                           @RequestParam("identityValue") String identityValue) {
        return userApplicationService.getUserIdentity(tenantId, identityType, identityValue);
    }

    @Operation(summary = "按租户 ID 查询租户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询租户详情", eventType = LogEventType.QUERY)
    @GetMapping("/tenants/{tenantId}")
    public TenantDTO getTenant(@PathVariable Long tenantId) {
        return userApplicationService.getTenantByTenantId(tenantId);
    }
}
