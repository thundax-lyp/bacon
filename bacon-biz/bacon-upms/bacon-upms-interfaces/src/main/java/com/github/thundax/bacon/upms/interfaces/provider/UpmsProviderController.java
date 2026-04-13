package com.github.thundax.bacon.upms.interfaces.provider;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.api.dto.UserPasswordChangeDTO;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import com.github.thundax.bacon.upms.application.command.RoleApplicationService;
import com.github.thundax.bacon.upms.application.command.UserApplicationService;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/providers/upms")
@Tag(name = "Inner-UPMS-Management", description = "UPMS 域内部 Provider 接口")
public class UpmsProviderController {

    private final UserApplicationService userApplicationService;
    private final DepartmentApplicationService departmentApplicationService;
    private final RoleApplicationService roleApplicationService;
    private final PermissionQueryApplicationService permissionQueryService;

    public UpmsProviderController(
            UserApplicationService userApplicationService,
            DepartmentApplicationService departmentApplicationService,
            RoleApplicationService roleApplicationService,
            PermissionQueryApplicationService permissionQueryService) {
        this.userApplicationService = userApplicationService;
        this.departmentApplicationService = departmentApplicationService;
        this.roleApplicationService = roleApplicationService;
        this.permissionQueryService = permissionQueryService;
    }

    @Operation(summary = "按用户 ID 查询用户")
    @GetMapping("/users/{userId}")
    public UserDTO getUserById(@PathVariable Long userId) {
        return userApplicationService.getUserById(userId);
    }

    @Operation(summary = "按身份标识查询用户身份")
    @GetMapping("/user-identities")
    public UserIdentityDTO getUserIdentity(
            @RequestParam("identityType") String identityType,
            @RequestParam("identityValue") String identityValue) {
        return userApplicationService.getUserIdentity(identityType, identityValue);
    }

    @Operation(summary = "按身份标识查询用户登录凭据")
    @GetMapping("/user-credentials")
    public UserLoginCredentialDTO getUserLoginCredential(
            @RequestParam("identityType") String identityType,
            @RequestParam("identityValue") String identityValue) {
        return userApplicationService.getUserLoginCredential(identityType, identityValue);
    }

    @Operation(summary = "当前用户修改密码")
    @PostMapping("/users/{userId}/password/change")
    public void changePassword(@PathVariable Long userId, @RequestBody UserPasswordChangeDTO request) {
        userApplicationService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
    }

    @Operation(summary = "按租户编号查询租户")
    @GetMapping("/tenants/{tenantId}")
    public TenantDTO getTenant(@PathVariable Long tenantId) {
        return userApplicationService.getTenantByTenantId(tenantId);
    }

    @Operation(summary = "按部门 ID 查询部门")
    @GetMapping("/departments/{departmentId}")
    public DepartmentDTO getDepartmentById(@PathVariable Long departmentId) {
        return departmentApplicationService.getDepartmentById(DepartmentId.of(departmentId));
    }

    @Operation(summary = "按部门编码查询部门")
    @GetMapping("/departments/code/{departmentCode}")
    public DepartmentDTO getDepartmentByCode(@PathVariable String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(departmentCode);
    }

    @Operation(summary = "批量查询部门")
    @GetMapping("/departments")
    public List<DepartmentDTO> listDepartmentsByIds(@RequestParam("departmentIds") Set<String> departmentIds) {
        Set<DepartmentId> resolvedDepartmentIds = departmentIds == null
                ? Set.of()
                : departmentIds.stream()
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(Long::parseLong)
                        .map(DepartmentId::of)
                        .collect(Collectors.toSet());
        return departmentApplicationService.listDepartmentsByIds(resolvedDepartmentIds);
    }

    @Operation(summary = "按角色 ID 查询角色")
    @GetMapping("/roles/{roleId}")
    public RoleDTO getRoleById(@PathVariable Long roleId) {
        return roleApplicationService.getRoleById(RoleId.of(roleId));
    }

    @Operation(summary = "查询用户角色列表")
    @GetMapping("/roles")
    public List<RoleDTO> getRolesByUserId(@RequestParam("userId") Long userId) {
        return roleApplicationService.getRolesByUserId(UserIdCodec.toDomain(userId));
    }

    @Operation(summary = "查询用户菜单树")
    @GetMapping("/permissions/menus")
    public List<UserMenuTreeDTO> getUserMenuTree(@RequestParam("userId") Long userId) {
        return permissionQueryService.getUserMenuTree(UserIdCodec.toDomain(userId));
    }

    @Operation(summary = "查询用户权限码")
    @GetMapping("/permissions/codes")
    public Set<String> getUserPermissionCodes(@RequestParam("userId") Long userId) {
        return permissionQueryService.getUserPermissionCodes(UserIdCodec.toDomain(userId));
    }

    @Operation(summary = "查询用户数据权限范围")
    @GetMapping("/permissions/data-scope")
    public UserDataScopeDTO getUserDataScope(@RequestParam("userId") Long userId) {
        return permissionQueryService.getUserDataScope(UserIdCodec.toDomain(userId));
    }
}
