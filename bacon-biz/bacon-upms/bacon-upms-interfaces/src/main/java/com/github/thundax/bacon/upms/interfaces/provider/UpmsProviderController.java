package com.github.thundax.bacon.upms.interfaces.provider;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserPasswordChangeDTO;
import com.github.thundax.bacon.upms.application.codec.DepartmentCodeCodec;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import com.github.thundax.bacon.upms.application.command.RoleApplicationService;
import com.github.thundax.bacon.upms.application.command.UserPasswordApplicationService;
import com.github.thundax.bacon.upms.application.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.application.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.application.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
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
    private final RoleApplicationService roleApplicationService;
    private final PermissionQueryApplicationService permissionQueryService;

    public UpmsProviderController(
            UserQueryApplicationService userQueryApplicationService,
            UserPasswordApplicationService userPasswordApplicationService,
            DepartmentApplicationService departmentApplicationService,
            RoleApplicationService roleApplicationService,
            PermissionQueryApplicationService permissionQueryService) {
        this.userQueryApplicationService = userQueryApplicationService;
        this.userPasswordApplicationService = userPasswordApplicationService;
        this.departmentApplicationService = departmentApplicationService;
        this.roleApplicationService = roleApplicationService;
        this.permissionQueryService = permissionQueryService;
    }

    @Operation(summary = "按用户 ID 查询用户")
    @GetMapping("/users/{userId}")
    public UserDTO getUserById(@PathVariable @Positive(message = "userId must be greater than 0") Long userId) {
        return userQueryApplicationService.getUserById(UserIdCodec.toDomain(userId));
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
    @PostMapping("/users/{userId}/passwords/change")
    public void changePassword(
            @PathVariable @Positive(message = "userId must be greater than 0") Long userId,
            @Valid @RequestBody UserPasswordChangeDTO request) {
        userPasswordApplicationService.changePassword(
                UserIdCodec.toDomain(userId), request.getOldPassword(), request.getNewPassword());
    }

    @Operation(summary = "按租户编号查询租户")
    @GetMapping("/tenants/{tenantId}")
    public TenantDTO getTenant(@PathVariable @Positive(message = "tenantId must be greater than 0") Long tenantId) {
        return userQueryApplicationService.getTenantByTenantId(TenantId.of(tenantId));
    }

    @Operation(summary = "按部门 ID 查询部门")
    @GetMapping("/departments/{departmentId}")
    public DepartmentDTO getDepartmentById(
            @PathVariable @Positive(message = "departmentId must be greater than 0") Long departmentId) {
        return departmentApplicationService.getDepartmentById(DepartmentId.of(departmentId));
    }

    @Operation(summary = "按部门编码查询部门")
    @GetMapping("/departments/code/{departmentCode}")
    public DepartmentDTO getDepartmentByCode(
            @PathVariable @NotBlank(message = "departmentCode must not be blank") String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(DepartmentCodeCodec.toDomain(departmentCode));
    }

    @Operation(summary = "批量查询部门")
    @GetMapping("/departments")
    public List<DepartmentDTO> listByIds(
            @RequestParam("departmentIds") Set<
                            @NotBlank(message = "departmentIds item must not be blank")
                            @Pattern(regexp = "\\d+", message = "departmentIds item must be numeric")
                            String>
                    departmentIds) {
        Set<DepartmentId> resolvedDepartmentIds = departmentIds == null
                ? Set.of()
                : departmentIds.stream()
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(Long::parseLong)
                        .map(DepartmentId::of)
                        .collect(Collectors.toSet());
        return departmentApplicationService.listByIds(resolvedDepartmentIds);
    }

    @Operation(summary = "按角色 ID 查询角色")
    @GetMapping("/roles/{roleId}")
    public RoleDTO getRoleById(@PathVariable @Positive(message = "roleId must be greater than 0") Long roleId) {
        return roleApplicationService.getRoleById(RoleId.of(roleId));
    }

    @Operation(summary = "查询用户角色列表")
    @GetMapping("/roles")
    public List<RoleDTO> getRolesByUserId(
            @RequestParam("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        return roleApplicationService.getRolesByUserId(UserIdCodec.toDomain(userId));
    }

    @Operation(summary = "查询用户菜单树")
    @GetMapping("/permissions/menus")
    public List<UserMenuTreeDTO> listMenuTreeByUserId(
            @RequestParam("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        return permissionQueryService.listMenuTreeByUserId(UserIdCodec.toDomain(userId));
    }

    @Operation(summary = "查询用户权限码")
    @GetMapping("/permissions/codes")
    public Set<String> findPermissionCodesByUserId(
            @RequestParam("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        return permissionQueryService.findPermissionCodesByUserId(UserIdCodec.toDomain(userId));
    }

    @Operation(summary = "查询用户数据权限范围")
    @GetMapping("/permissions/data-scope")
    public UserDataScopeDTO getUserDataScope(
            @RequestParam("userId") @Positive(message = "userId must be greater than 0") Long userId) {
        return permissionQueryService.getUserDataScope(UserIdCodec.toDomain(userId));
    }
}
