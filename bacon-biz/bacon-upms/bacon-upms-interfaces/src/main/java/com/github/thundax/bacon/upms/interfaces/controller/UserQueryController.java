package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.interfaces.request.UserIdentityQueryRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserPageRequest;
import com.github.thundax.bacon.upms.interfaces.response.RoleResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserIdentityResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/users")
@Validated
@Tag(name = "UPMS-User", description = "用户查询接口")
public class UserQueryController {

    private final UserQueryApplicationService userQueryApplicationService;

    public UserQueryController(UserQueryApplicationService userQueryApplicationService) {
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
