package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.interfaces.assembler.UserInterfaceAssembler;
import com.github.thundax.bacon.upms.application.command.UserAvatarApplicationService;
import com.github.thundax.bacon.upms.interfaces.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@WrappedApiController
@RequestMapping("/upms/users")
@Validated
@Tag(name = "UPMS-User", description = "用户头像管理接口")
public class UserAvatarController {

    private final UserAvatarApplicationService userAvatarApplicationService;

    public UserAvatarController(UserAvatarApplicationService userAvatarApplicationService) {
        this.userAvatarApplicationService = userAvatarApplicationService;
    }

    @Operation(summary = "上传用户头像")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "上传用户头像", eventType = LogEventType.UPDATE)
    @PutMapping(value = "/{userId}/avatar", consumes = "multipart/form-data")
    public UserResponse uploadAvatar(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @RequestParam("file") @NotNull(message = "file must not be null") MultipartFile file)
            throws IOException {
        return UserInterfaceAssembler.toResponse(
                userAvatarApplicationService.updateAvatar(UserInterfaceAssembler.toAvatarUpdateCommand(userId, file)));
    }
}
