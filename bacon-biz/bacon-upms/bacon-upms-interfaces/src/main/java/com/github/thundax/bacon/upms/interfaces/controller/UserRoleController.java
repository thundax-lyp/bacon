package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.interfaces.assembler.UserInterfaceAssembler;
import com.github.thundax.bacon.upms.application.command.UserProfileApplicationService;
import com.github.thundax.bacon.upms.interfaces.request.UserRoleAssignRequest;
import com.github.thundax.bacon.upms.interfaces.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
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
@Tag(name = "UPMS-User", description = "用户角色管理接口")
public class UserRoleController {

    private final UserProfileApplicationService userProfileApplicationService;

    public UserRoleController(UserProfileApplicationService userProfileApplicationService) {
        this.userProfileApplicationService = userProfileApplicationService;
    }

    @Operation(summary = "分配用户角色")
    @HasPermission("sys:user:update")
    @SysLog(module = "UPMS", action = "分配用户角色", eventType = LogEventType.GRANT)
    @PutMapping("/{userId}/roles")
    public List<RoleResponse> updateRoleIds(
            @PathVariable("userId") @Positive(message = "userId must be greater than 0") Long userId,
            @Valid @RequestBody UserRoleAssignRequest request) {
        return UserInterfaceAssembler.toRoleResponseList(
                userProfileApplicationService.updateRoleIds(UserInterfaceAssembler.toRoleAssignCommand(userId, request)));
    }
}
