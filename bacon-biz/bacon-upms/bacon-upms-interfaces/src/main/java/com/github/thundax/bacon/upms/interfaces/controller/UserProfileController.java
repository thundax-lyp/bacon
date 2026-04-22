package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.interfaces.assembler.UserInterfaceAssembler;
import com.github.thundax.bacon.upms.application.command.UserImportCommand;
import com.github.thundax.bacon.upms.application.command.UserProfileApplicationService;
import com.github.thundax.bacon.upms.interfaces.request.UserCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserImportItemRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserImportRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserStatusUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.UserUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/users")
@Validated
@Tag(name = "UPMS-User", description = "用户资料管理接口")
public class UserProfileController {

    private final UserProfileApplicationService userProfileApplicationService;

    public UserProfileController(UserProfileApplicationService userProfileApplicationService) {
        this.userProfileApplicationService = userProfileApplicationService;
    }

    @Operation(summary = "创建用户")
    @HasPermission("sys:user:create")
    @SysLog(module = "UPMS", action = "创建用户", eventType = LogEventType.CREATE)
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        return UserInterfaceAssembler.toResponse(
                userProfileApplicationService.create(UserInterfaceAssembler.toCreateCommand(request)));
    }

    @Operation(summary = "修改用户基本信息")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "修改用户基本信息", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return UserInterfaceAssembler.toResponse(
                userProfileApplicationService.update(UserInterfaceAssembler.toUpdateCommand(userId, request)));
    }

    @Operation(summary = "启用或停用用户")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "变更用户状态", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/status")
    public UserResponse updateUserStatus(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        return UserInterfaceAssembler.toResponse(
                userProfileApplicationService.updateStatus(
                        UserInterfaceAssembler.toStatusUpdateCommand(userId, request)));
    }

    @Operation(summary = "删除用户")
    @HasPermission("sys:user:delete")
    @SysLog(module = "UPMS", action = "删除用户", eventType = LogEventType.DELETE)
    @DeleteMapping("/{userId}")
    public void delete(@PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        userProfileApplicationService.delete(UserIdCodec.toDomain(userId));
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
}
