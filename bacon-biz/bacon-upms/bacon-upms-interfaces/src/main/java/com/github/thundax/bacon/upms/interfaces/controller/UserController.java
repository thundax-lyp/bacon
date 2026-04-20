package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.command.UserAvatarApplicationService;
import com.github.thundax.bacon.upms.application.command.UserImportCommand;
import com.github.thundax.bacon.upms.application.command.UserPasswordApplicationService;
import com.github.thundax.bacon.upms.application.command.UserProfileApplicationService;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.interfaces.request.UserCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserIdentityQueryRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserImportItemRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserImportRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserPageRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserPasswordResetRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserRoleAssignRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.RoleResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserIdentityResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@RestController
@WrappedApiController
@RequestMapping("/upms/users")
@Validated
@Tag(name = "UPMS-User", description = "用户管理接口")
public class UserController {

    private final UserProfileApplicationService userProfileApplicationService;
    private final UserPasswordApplicationService userPasswordApplicationService;
    private final UserAvatarApplicationService userAvatarApplicationService;
    private final UserQueryApplicationService userQueryApplicationService;

    public UserController(
            UserProfileApplicationService userProfileApplicationService,
            UserPasswordApplicationService userPasswordApplicationService,
            UserAvatarApplicationService userAvatarApplicationService,
            UserQueryApplicationService userQueryApplicationService) {
        this.userProfileApplicationService = userProfileApplicationService;
        this.userPasswordApplicationService = userPasswordApplicationService;
        this.userAvatarApplicationService = userAvatarApplicationService;
        this.userQueryApplicationService = userQueryApplicationService;
    }

    @Operation(summary = "分页查询用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "分页查询用户", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public UserPageResponse page(@Valid @ModelAttribute UserPageRequest request) {
        return UserPageResponse.from(userQueryApplicationService.page(
                request.getAccount(),
                request.getName(),
                request.getPhone(),
                request.getStatus() == null ? null : UserStatus.from(request.getStatus()),
                request.getPageNo(),
                request.getPageSize()));
    }

    @Operation(summary = "创建用户")
    @HasPermission("sys:user:create")
    @SysLog(module = "UPMS", action = "创建用户", eventType = LogEventType.CREATE)
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        return UserResponse.from(userProfileApplicationService.createUser(
                request.account(), request.name(), request.phone(), DepartmentId.of(request.departmentId())));
    }

    @Operation(summary = "修改用户基本信息")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "修改用户基本信息", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return UserResponse.from(userProfileApplicationService.updateUser(
                UserIdCodec.toDomain(userId),
                request.account(),
                request.name(),
                request.phone(),
                DepartmentId.of(request.departmentId())));
    }

    @Operation(summary = "按用户 ID 查询用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "查询用户详情", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}")
    public UserResponse getUserById(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        return UserResponse.from(userQueryApplicationService.getUserById(UserIdCodec.toDomain(userId)));
    }

    @Operation(summary = "访问用户头像")
    @SysLog(module = "UPMS", action = "访问用户头像", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}/avatar")
    public ResponseEntity<Void> getAvatar(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        Optional<String> avatarAccessUrl = userQueryApplicationService.getAvatarAccessUrl(UserIdCodec.toDomain(userId));
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
    public UserIdentityResponse getUserIdentity(@Valid @ModelAttribute UserIdentityQueryRequest request) {
        return UserIdentityResponse.from(userQueryApplicationService.getUserIdentity(
                UserIdentityType.from(request.getIdentityType()), request.getIdentityValue()));
    }

    @Operation(summary = "启用或停用用户")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "变更用户状态", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/status")
    public UserResponse updateUserStatus(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        return UserResponse.from(userProfileApplicationService.updateUserStatus(
                UserIdCodec.toDomain(userId), request.status() == null ? null : UserStatus.from(request.status())));
    }

    @Operation(summary = "删除用户")
    @HasPermission("sys:user:delete")
    @SysLog(module = "UPMS", action = "删除用户", eventType = LogEventType.DELETE)
    @DeleteMapping("/{userId}")
    public void delete(@PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        userProfileApplicationService.delete(UserIdCodec.toDomain(userId));
    }

    @Operation(summary = "管理员初始化密码")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "初始化用户密码", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/passwords/init")
    public UserResponse initPassword(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        return UserResponse.from(userPasswordApplicationService.initPassword(UserIdCodec.toDomain(userId)));
    }

    @Operation(summary = "管理员重置密码")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "重置用户密码", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/passwords/reset")
    public UserResponse resetPassword(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @Valid @RequestBody UserPasswordResetRequest request) {
        return UserResponse.from(
                userPasswordApplicationService.resetPassword(UserIdCodec.toDomain(userId), request.newPassword()));
    }

    @Operation(summary = "分配用户角色")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "分配用户角色", eventType = LogEventType.GRANT)
    @PutMapping("/{userId}/roles")
    public List<RoleResponse> updateRoleIds(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @Valid @RequestBody UserRoleAssignRequest request) {
        return userProfileApplicationService
                .updateRoleIds(
                        UserIdCodec.toDomain(userId),
                        request.roleIds() == null
                                ? List.of()
                                : request.roleIds().stream().map(RoleId::of).toList())
                .stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Operation(summary = "上传用户头像")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "上传用户头像", eventType = LogEventType.UPDATE)
    @PutMapping(value = "/{userId}/avatar", consumes = "multipart/form-data")
    public UserResponse uploadAvatar(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @RequestParam("file") @NotNull(message = "file must not be null") MultipartFile file)
            throws IOException {
        return UserResponse.from(userAvatarApplicationService.updateAvatar(
                UserIdCodec.toDomain(userId),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream()));
    }

    @Operation(summary = "查询用户角色列表")
    @HasPermission("sys:role:view")
    @SysLog(module = "UPMS", action = "查询用户角色", eventType = LogEventType.QUERY)
    @GetMapping("/{userId}/roles")
    public List<RoleResponse> getRolesByUserId(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        return userQueryApplicationService.getRolesByUserId(UserIdCodec.toDomain(userId)).stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Operation(summary = "导入用户")
    @HasPermission("sys:user:create")
    @SysLog(module = "UPMS", action = "导入用户", eventType = LogEventType.IMPORT)
    @PostMapping("/import")
    public List<UserResponse> importUsers(@Valid @RequestBody UserImportRequest request) {
        List<UserImportItemRequest> items = request.items() == null ? List.of() : request.items();
        return userProfileApplicationService
                .importUsers(items.stream()
                        .map(item ->
                                new UserImportCommand(item.account(), item.name(), item.phone(), item.departmentCode()))
                        .toList())
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Operation(summary = "导出用户")
    @HasPermission("sys:user:view")
    @SysLog(module = "UPMS", action = "导出用户", eventType = LogEventType.EXPORT)
    @GetMapping("/export")
    public List<UserResponse> exportUsers(@Valid @ModelAttribute UserPageRequest request) {
        return userQueryApplicationService
                .exportUsers(
                        request.getAccount(),
                        request.getName(),
                        request.getPhone(),
                        request.getStatus() == null ? null : UserStatus.from(request.getStatus()))
                .stream()
                .map(UserResponse::from)
                .toList();
    }
}
