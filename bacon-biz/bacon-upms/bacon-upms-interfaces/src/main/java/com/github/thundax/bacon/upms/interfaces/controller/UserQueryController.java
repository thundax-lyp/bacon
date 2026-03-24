package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.service.UserApplicationService;
import com.github.thundax.bacon.upms.interfaces.response.TenantResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserIdentityResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/users")
@Tag(name = "UPMS-User", description = "用户查询接口")
public class UserQueryController {

    private final UserApplicationService userApplicationService;

    public UserQueryController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Operation(summary = "按用户 ID 查询用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询用户详情", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}")
    public UserResponse getUserById(@RequestParam("tenantId") Long tenantId, @PathVariable Long userId) {
        return UserResponse.from(userApplicationService.getUserById(tenantId, userId));
    }

    @Operation(summary = "按身份标识查询用户身份")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询用户身份", eventType = LogEventType.QUERY)
    @GetMapping("/identity")
    public UserIdentityResponse getUserIdentity(@RequestParam("tenantId") Long tenantId,
                                                @RequestParam("identityType") String identityType,
                                                @RequestParam("identityValue") String identityValue) {
        return UserIdentityResponse.from(userApplicationService.getUserIdentity(tenantId, identityType, identityValue));
    }

    @Operation(summary = "按租户 ID 查询租户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询租户详情", eventType = LogEventType.QUERY)
    @GetMapping("/tenants/{tenantId}")
    public TenantResponse getTenant(@PathVariable Long tenantId) {
        return TenantResponse.from(userApplicationService.getTenantByTenantId(tenantId));
    }
}
