package com.github.thundax.bacon.upms.interfaces.provider;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;
import com.github.thundax.bacon.upms.application.codec.DepartmentCodeCodec;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import com.github.thundax.bacon.upms.application.command.UserPasswordApplicationService;
import com.github.thundax.bacon.upms.application.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.application.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final DepartmentApplicationService departmentApplicationService;
    private final PermissionQueryApplicationService permissionQueryService;

    public UpmsProviderController(
            UserQueryApplicationService userQueryApplicationService,
            UserPasswordApplicationService userPasswordApplicationService,
            DepartmentApplicationService departmentApplicationService,
            PermissionQueryApplicationService permissionQueryService) {
        this.userQueryApplicationService = userQueryApplicationService;
        this.userPasswordApplicationService = userPasswordApplicationService;
        this.departmentApplicationService = departmentApplicationService;
        this.permissionQueryService = permissionQueryService;
    }

    @Operation(summary = "查询当前用户")
    @GetMapping("/users/current")
    public UserFacadeResponse getCurrentUser() {
        UserDTO user = userQueryApplicationService.getUserById(BaconIdContextHelper.requireUserId());
        String departmentCode = user.getDepartmentId() == null
                ? null
                : departmentApplicationService
                        .getDepartmentById(DepartmentIdCodec.toDomain(user.getDepartmentId()))
                        .getCode();
        return new UserFacadeResponse(
                user.getId(), user.getAccount(), user.getName(), user.getAvatarStoredObjectNo(), user.getPhone(),
                departmentCode, user.getAvatarUrl(), user.getStatus());
    }

    @Operation(summary = "按身份标识查询用户身份")
    @GetMapping("/user-identities")
    public UserIdentityDTO getUserIdentity(
            @RequestParam("identityType") @NotBlank(message = "identityType must not be blank") String identityType,
            @RequestParam("identityValue") @NotBlank(message = "identityValue must not be blank")
                    String identityValue) {
        return userQueryApplicationService.getUserIdentity(UserIdentityType.from(identityType), identityValue);
    }

    @Operation(summary = "按身份标识查询用户登录凭据")
    @GetMapping("/user-credentials")
    public UserLoginCredentialDTO getUserLoginCredential(
            @RequestParam("identityType") @NotBlank(message = "identityType must not be blank") String identityType,
            @RequestParam("identityValue") @NotBlank(message = "identityValue must not be blank")
                    String identityValue) {
        return userQueryApplicationService.getUserLoginCredential(UserIdentityType.from(identityType), identityValue);
    }

    @Operation(summary = "当前用户修改密码")
    @PostMapping("/users/current/passwords/change")
    public void changePassword(@Valid @RequestBody UserPasswordChangeFacadeRequest request) {
        userPasswordApplicationService.changePassword(
                BaconIdContextHelper.requireUserId(),
                request.getOldPassword(),
                request.getNewPassword());
    }

    @Operation(summary = "按租户编号查询租户")
    @GetMapping("/tenants/{tenantId}")
    public TenantDTO getTenant(@PathVariable @Positive(message = "tenantId must be greater than 0") Long tenantId) {
        return userQueryApplicationService.getTenantByTenantId(TenantId.of(tenantId));
    }

    @Operation(summary = "按部门编码查询部门")
    @GetMapping("/departments/code/{code}")
    public DepartmentDTO getByCode(
            @PathVariable("code") @NotBlank(message = "code must not be blank") String code) {
        return departmentApplicationService.getDepartmentByCode(DepartmentCodeCodec.toDomain(code));
    }

    @Operation(summary = "批量查询部门")
    @GetMapping("/departments")
    public List<DepartmentDTO> listByIds(
            @RequestParam("departmentIds") Set<
                            @Positive(message = "departmentIds item must be greater than 0")
                            Long>
                    departmentIds) {
        Set<DepartmentId> resolvedDepartmentIds = departmentIds == null
                ? Set.of()
                : departmentIds.stream()
                        .map(DepartmentId::of)
                        .collect(Collectors.toSet());
        return departmentApplicationService.listByIds(resolvedDepartmentIds);
    }

    @Operation(summary = "查询用户数据权限范围")
    @GetMapping("/permissions/data-scope")
    public UserDataScopeDTO getUserDataScope(
            @RequestParam("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        return permissionQueryService.getUserDataScope(UserIdCodec.toDomain(userId));
    }

    @Operation(summary = "查询当前用户数据权限范围")
    @GetMapping("/permissions/current/data-scope")
    public UserDataScopeDTO getCurrentDataScope() {
        return permissionQueryService.getUserDataScope(BaconIdContextHelper.requireUserId());
    }
}
