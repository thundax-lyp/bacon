package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.command.UserImportCommand;
import com.github.thundax.bacon.upms.api.dto.UserPageQueryDTO;
import com.github.thundax.bacon.upms.application.service.UserApplicationService;
import com.github.thundax.bacon.upms.interfaces.dto.TenantScopedRequest;
import com.github.thundax.bacon.upms.interfaces.dto.UserCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.UserImportItem;
import com.github.thundax.bacon.upms.interfaces.dto.UserImportRequest;
import com.github.thundax.bacon.upms.interfaces.dto.UserIdentityQueryRequest;
import com.github.thundax.bacon.upms.interfaces.dto.UserPageRequest;
import com.github.thundax.bacon.upms.interfaces.dto.UserPasswordInitRequest;
import com.github.thundax.bacon.upms.interfaces.dto.UserPasswordResetRequest;
import com.github.thundax.bacon.upms.interfaces.dto.UserRoleAssignRequest;
import com.github.thundax.bacon.upms.interfaces.dto.UserStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.UserUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.RoleResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserIdentityResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/upms/users")
@Tag(name = "UPMS-User", description = "用户管理接口")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Operation(summary = "分页查询用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "分页查询用户", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public UserPageResponse pageUsers(@Valid @ModelAttribute UserPageRequest request) {
        return UserPageResponse.from(userApplicationService.pageUsers(new UserPageQueryDTO(request.getTenantId(),
                request.getAccount(), request.getName(), request.getPhone(), request.getStatus(), request.getPageNo(),
                request.getPageSize())));
    }

    @Operation(summary = "创建用户")
    @HasPermission("sys:user:create")
    @SysLog(module = "UPMS", action = "创建用户", eventType = LogEventType.CREATE)
    @PostMapping
    public UserResponse createUser(@RequestBody UserCreateRequest request) {
        return UserResponse.from(userApplicationService.createUser(request.tenantId(), request.account(), request.name(),
                request.phone(), request.departmentId()));
    }

    @Operation(summary = "修改用户基本信息")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "修改用户基本信息", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}")
    public UserResponse updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        return UserResponse.from(userApplicationService.updateUser(request.tenantId(), userId, request.account(),
                request.name(), request.phone(), request.departmentId()));
    }

    @Operation(summary = "按用户 ID 查询用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询用户详情", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId, @ModelAttribute TenantScopedRequest request) {
        return UserResponse.from(userApplicationService.getUserById(request.getTenantId(), userId));
    }

    @Operation(summary = "按身份标识查询用户身份")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询用户身份", eventType = LogEventType.QUERY)
    @GetMapping("/identity")
    public UserIdentityResponse getUserIdentity(@ModelAttribute UserIdentityQueryRequest request) {
        return UserIdentityResponse.from(userApplicationService.getUserIdentity(request.getTenantId(),
                request.getIdentityType(), request.getIdentityValue()));
    }

    @Operation(summary = "启用或停用用户")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "变更用户状态", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/status")
    public UserResponse updateUserStatus(@PathVariable Long userId, @RequestBody UserStatusUpdateRequest request) {
        return UserResponse.from(userApplicationService.updateUserStatus(request.tenantId(), userId, request.status()));
    }

    @Operation(summary = "删除用户")
    @HasPermission("sys:user:delete")
    @SysLog(module = "UPMS", action = "删除用户", eventType = LogEventType.DELETE)
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId, @ModelAttribute TenantScopedRequest request) {
        userApplicationService.deleteUser(request.getTenantId(), userId);
    }

    @Operation(summary = "管理员初始化密码")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "初始化用户密码", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/password/init")
    public UserResponse initPassword(@PathVariable Long userId, @RequestBody UserPasswordInitRequest request) {
        return UserResponse.from(userApplicationService.initPassword(request.tenantId(), userId));
    }

    @Operation(summary = "管理员重置密码")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "重置用户密码", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/password/reset")
    public UserResponse resetPassword(@PathVariable Long userId, @RequestBody UserPasswordResetRequest request) {
        return UserResponse.from(userApplicationService.resetPassword(request.tenantId(), userId, request.newPassword()));
    }

    @Operation(summary = "分配用户角色")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "分配用户角色", eventType = LogEventType.GRANT)
    @PutMapping("/{userId}/roles")
    public List<RoleResponse> assignRoles(@PathVariable Long userId, @RequestBody UserRoleAssignRequest request) {
        return userApplicationService.assignRoles(request.tenantId(), userId, request.roleIds()).stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Operation(summary = "查询用户角色列表")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询用户角色", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}/roles")
    public List<RoleResponse> getRolesByUserId(@PathVariable Long userId, @ModelAttribute TenantScopedRequest request) {
        return userApplicationService.getRolesByUserId(request.getTenantId(), userId).stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Operation(summary = "导入用户")
    @HasPermission("sys:user:create")
    @SysLog(module = "UPMS", action = "导入用户", eventType = LogEventType.IMPORT)
    @PostMapping("/import")
    public List<UserResponse> importUsers(@RequestBody UserImportRequest request) {
        List<UserImportItem> items = request.items() == null ? List.of() : request.items();
        return userApplicationService.importUsers(request.tenantId(), items.stream()
                        .map(item -> new UserImportCommand(item.account(), item.name(), item.phone(),
                                item.departmentId()))
                        .toList())
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Operation(summary = "导出用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "导出用户", eventType = LogEventType.EXPORT)
    @GetMapping("/export")
    public List<UserResponse> exportUsers(@ModelAttribute UserPageRequest request) {
        return userApplicationService.exportUsers(new UserPageQueryDTO(request.getTenantId(), request.getAccount(),
                request.getName(), request.getPhone(), request.getStatus(), 1, Integer.MAX_VALUE))
                .stream()
                .map(UserResponse::from)
                .toList();
    }

}
