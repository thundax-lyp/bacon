package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.command.UserImportCommand;
import com.github.thundax.bacon.upms.api.dto.UserPageQueryDTO;
import com.github.thundax.bacon.upms.application.command.UserApplicationService;
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
import com.github.thundax.bacon.upms.interfaces.resolver.TenantRequestResolver;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@WrappedApiController
@RequestMapping("/upms/users")
@Tag(name = "UPMS-User", description = "用户管理接口")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final TenantRequestResolver tenantRequestResolver;

    public UserController(UserApplicationService userApplicationService,
                          TenantRequestResolver tenantRequestResolver) {
        this.userApplicationService = userApplicationService;
        this.tenantRequestResolver = tenantRequestResolver;
    }

    @Operation(summary = "分页查询用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "分页查询用户", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public UserPageResponse pageUsers(@Valid @ModelAttribute UserPageRequest request) {
        return UserPageResponse.from(userApplicationService.pageUsers(new UserPageQueryDTO(
                tenantRequestResolver.resolveTenantId(request.getTenantId()),
                request.getAccount(), request.getName(), request.getPhone(),
                request.getStatus() == null ? null : request.getStatus().name(), request.getPageNo(),
                request.getPageSize())));
    }

    @Operation(summary = "创建用户")
    @HasPermission("sys:user:create")
    @SysLog(module = "UPMS", action = "创建用户", eventType = LogEventType.CREATE)
    @PostMapping
    public UserResponse createUser(@RequestBody UserCreateRequest request) {
        return UserResponse.from(userApplicationService.createUser(
                tenantRequestResolver.resolveTenantId(request.tenantId()), request.account(), request.name(),
                request.phone(), request.departmentId()));
    }

    @Operation(summary = "修改用户基本信息")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "修改用户基本信息", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}")
    public UserResponse updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return UserResponse.from(userApplicationService.updateUser(
                tenantRequestResolver.resolveTenantId(request.tenantId()), userId, request.account(),
                request.name(), request.phone(), request.departmentId()));
    }

    @Operation(summary = "按用户 ID 查询用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询用户详情", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable String userId, @ModelAttribute TenantScopedRequest request) {
        return UserResponse.from(userApplicationService.getUserById(request.getTenantId(), userId));
    }

    @Operation(summary = "访问用户头像")
    @SysLog(module = "UPMS", action = "访问用户头像", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}/avatar")
    public ResponseEntity<Void> getAvatar(@PathVariable("userId") String userId, @ModelAttribute TenantScopedRequest request) {
        java.util.Optional<String> avatarAccessUrl = userApplicationService.getAvatarAccessUrl(
                tenantRequestResolver.resolveTenantId(request.getTenantId()), userId);
        if (avatarAccessUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create(avatarAccessUrl.get()).toString())
                .build();
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
    public UserResponse updateUserStatus(@PathVariable String userId, @RequestBody UserStatusUpdateRequest request) {
        return UserResponse.from(userApplicationService.updateUserStatus(
                tenantRequestResolver.resolveTenantId(request.tenantId()), userId, request.status()));
    }

    @Operation(summary = "删除用户")
    @HasPermission("sys:user:delete")
    @SysLog(module = "UPMS", action = "删除用户", eventType = LogEventType.DELETE)
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId, @ModelAttribute TenantScopedRequest request) {
        userApplicationService.deleteUser(tenantRequestResolver.resolveTenantId(request.getTenantId()), userId);
    }

    @Operation(summary = "管理员初始化密码")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "初始化用户密码", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/password/init")
    public UserResponse initPassword(@PathVariable String userId, @RequestBody UserPasswordInitRequest request) {
        return UserResponse.from(userApplicationService.initPassword(
                tenantRequestResolver.resolveTenantId(request.tenantId()), userId));
    }

    @Operation(summary = "管理员重置密码")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "重置用户密码", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/password/reset")
    public UserResponse resetPassword(@PathVariable String userId, @RequestBody UserPasswordResetRequest request) {
        return UserResponse.from(userApplicationService.resetPassword(
                tenantRequestResolver.resolveTenantId(request.tenantId()), userId, request.newPassword()));
    }

    @Operation(summary = "分配用户角色")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "分配用户角色", eventType = LogEventType.GRANT)
    @PutMapping("/{userId}/roles")
    public List<RoleResponse> assignRoles(@PathVariable String userId, @RequestBody UserRoleAssignRequest request) {
        return userApplicationService.assignRoles(
                        tenantRequestResolver.resolveTenantId(request.tenantId()), userId, request.roleIds()).stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Operation(summary = "上传用户头像")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "上传用户头像", eventType = LogEventType.UPDATE)
    @PutMapping(value = "/{userId}/avatar", consumes = "multipart/form-data")
    public UserResponse uploadAvatar(@PathVariable("userId") String userId,
                                     @RequestParam("tenantId") String tenantId,
                                     @RequestParam("file") MultipartFile file) throws IOException {
        return UserResponse.from(userApplicationService.updateAvatar(
                tenantRequestResolver.resolveTenantId(tenantId), userId, file.getOriginalFilename(),
                file.getContentType(), file.getSize(), file.getInputStream()));
    }

    @Operation(summary = "查询用户角色列表")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询用户角色", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}/roles")
    public List<RoleResponse> getRolesByUserId(@PathVariable String userId, @ModelAttribute TenantScopedRequest request) {
        return userApplicationService.getRolesByUserId(
                        tenantRequestResolver.resolveTenantId(request.getTenantId()), userId).stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Operation(summary = "导入用户")
    @HasPermission("sys:user:create")
    @SysLog(module = "UPMS", action = "导入用户", eventType = LogEventType.IMPORT)
    @PostMapping("/import")
    public List<UserResponse> importUsers(@RequestBody UserImportRequest request) {
        List<UserImportItem> items = request.items() == null ? List.of() : request.items();
        return userApplicationService.importUsers(tenantRequestResolver.resolveTenantId(request.tenantId()), items.stream()
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
        return userApplicationService.exportUsers(new UserPageQueryDTO(
                tenantRequestResolver.resolveTenantId(request.getTenantId()), request.getAccount(),
                request.getName(), request.getPhone(), request.getStatus() == null ? null : request.getStatus().name(),
                1, Integer.MAX_VALUE))
                .stream()
                .map(UserResponse::from)
                .toList();
    }

}
