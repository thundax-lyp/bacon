package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.interfaces.assembler.UserInterfaceAssembler;
import com.github.thundax.bacon.upms.application.command.UserPasswordApplicationService;
import com.github.thundax.bacon.upms.interfaces.request.UserPasswordResetRequest;
import com.github.thundax.bacon.upms.interfaces.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/users")
@Validated
@Tag(name = "UPMS-User", description = "用户密码管理接口")
public class UserPasswordController {

    private final UserPasswordApplicationService userPasswordApplicationService;

    public UserPasswordController(UserPasswordApplicationService userPasswordApplicationService) {
        this.userPasswordApplicationService = userPasswordApplicationService;
    }

    @Operation(summary = "管理员初始化密码")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "初始化用户密码", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/passwords/init")
    public UserResponse initPassword(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        return UserInterfaceAssembler.toResponse(userPasswordApplicationService.initPassword(UserIdCodec.toDomain(userId)));
    }

    @Operation(summary = "管理员重置密码")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "重置用户密码", eventType = LogEventType.UPDATE)
    @PutMapping("/{userId}/passwords/reset")
    public UserResponse resetPassword(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @Valid @RequestBody UserPasswordResetRequest request) {
        return UserInterfaceAssembler.toResponse(
                userPasswordApplicationService.resetPassword(
                        UserInterfaceAssembler.toPasswordResetCommand(userId, request)));
    }
}
