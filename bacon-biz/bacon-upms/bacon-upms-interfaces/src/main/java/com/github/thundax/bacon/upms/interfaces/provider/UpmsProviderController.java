package com.github.thundax.bacon.upms.interfaces.provider;

import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.upms.api.request.UserCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.interfaces.assembler.UserInterfaceAssembler;
import com.github.thundax.bacon.upms.application.command.UserPasswordApplicationService;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.interfaces.assembler.TenantInterfaceAssembler;
import com.github.thundax.bacon.upms.application.query.DepartmentQueryApplicationService;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.request.UserPasswordChangeRequest;
import com.github.thundax.bacon.upms.interfaces.response.CurrentUserResponse;
import com.github.thundax.bacon.upms.interfaces.response.TenantResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserCredentialResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserDataScopeResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserIdentityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/providers/upms")
@Validated
@Tag(name = "Inner-UPMS-Management", description = "UPMS 域内部 Provider 接口")
public class UpmsProviderController {

    private final UserQueryApplicationService userQueryApplicationService;
    private final UserPasswordApplicationService userPasswordApplicationService;
    private final DepartmentQueryApplicationService departmentQueryApplicationService;
    private final PermissionQueryApplicationService permissionQueryService;

    public UpmsProviderController(
            UserQueryApplicationService userQueryApplicationService,
            UserPasswordApplicationService userPasswordApplicationService,
            DepartmentQueryApplicationService departmentQueryApplicationService,
            PermissionQueryApplicationService permissionQueryService) {
        this.userQueryApplicationService = userQueryApplicationService;
        this.userPasswordApplicationService = userPasswordApplicationService;
        this.departmentQueryApplicationService = departmentQueryApplicationService;
        this.permissionQueryService = permissionQueryService;
    }

    @Operation(summary = "查询当前用户")
    @GetMapping("/queries/current-user")
    public CurrentUserResponse getCurrentUser() {
        UserDTO user = userQueryApplicationService.getById(BaconIdContextHelper.requireUserId());
        String departmentCode = user.getDepartmentId() == null
                ? null
                : departmentQueryApplicationService
                        .getById(DepartmentIdCodec.toDomain(user.getDepartmentId()))
                        .getCode();
        return UserInterfaceAssembler.toCurrentUserResponse(user, departmentCode);
    }

    @Operation(summary = "按身份标识查询用户身份")
    @GetMapping("/queries/user-identity")
    public UserIdentityResponse getUserIdentity(
            @RequestParam("identityType") @NotBlank(message = "identityType must not be blank") String identityType,
            @RequestParam("identityValue") @NotBlank(message = "identityValue must not be blank")
                    String identityValue) {
        return UserInterfaceAssembler.toResponse(userQueryApplicationService.getUserIdentity(
                UserInterfaceAssembler.toIdentityQuery(new UserIdentityGetFacadeRequest(identityType, identityValue))));
    }

    @Operation(summary = "按身份标识查询用户登录凭据")
    @GetMapping("/queries/user-credential")
    public UserCredentialResponse getUserLoginCredential(
            @RequestParam("identityType") @NotBlank(message = "identityType must not be blank") String identityType,
            @RequestParam("identityValue") @NotBlank(message = "identityValue must not be blank")
                    String identityValue) {
        return UserInterfaceAssembler.toResponse(userQueryApplicationService.getUserLoginCredential(
                UserInterfaceAssembler.toLoginCredentialQuery(
                        new UserCredentialGetFacadeRequest(identityType, identityValue))));
    }

    @Operation(summary = "当前用户修改密码")
    @PostMapping("/commands/change-current-user-password")
    public void changePassword(@Valid @RequestBody UserPasswordChangeRequest request) {
        userPasswordApplicationService.changePassword(
                UserInterfaceAssembler.toPasswordChangeCommand(BaconIdContextHelper.requireUserId(), request));
    }

    @Operation(summary = "查询当前租户")
    @GetMapping("/queries/current-tenant")
    public TenantResponse getCurrentTenant() {
        return TenantInterfaceAssembler.toResponse(
                userQueryApplicationService.getTenantById(BaconIdContextHelper.requireTenantId()));
    }

    @Operation(summary = "查询当前用户数据权限范围")
    @GetMapping("/queries/current-data-scope")
    public UserDataScopeResponse getCurrentDataScope() {
        var dataScope = permissionQueryService.getUserDataScope(BaconIdContextHelper.requireUserId());
        return new UserDataScopeResponse(
                dataScope.isAllAccess(), dataScope.getScopeTypes(), dataScope.getDepartmentIds());
    }

}
