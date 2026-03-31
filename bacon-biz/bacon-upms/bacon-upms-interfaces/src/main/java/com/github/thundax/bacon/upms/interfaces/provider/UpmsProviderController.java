package com.github.thundax.bacon.upms.interfaces.provider;

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
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import com.github.thundax.bacon.upms.application.command.RoleApplicationService;
import com.github.thundax.bacon.upms.application.command.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
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

    public UpmsProviderController(UserApplicationService userApplicationService,
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
    public UserDTO getUserById(@RequestParam("tenantNo") String tenantNo, @PathVariable Long userId) {
        return userApplicationService.getUserById(tenantNo, userId);
    }

    @Operation(summary = "按身份标识查询用户身份")
    @GetMapping("/user-identities")
    public UserIdentityDTO getUserIdentity(@RequestParam("tenantNo") String tenantNo,
                                           @RequestParam("identityType") String identityType,
                                           @RequestParam("identityValue") String identityValue) {
        return userApplicationService.getUserIdentity(tenantNo, identityType, identityValue);
    }

    @Operation(summary = "按身份标识查询用户登录凭据")
    @GetMapping("/user-credentials")
    public UserLoginCredentialDTO getUserLoginCredential(@RequestParam("tenantNo") String tenantNo,
                                                         @RequestParam("identityType") String identityType,
                                                         @RequestParam("identityValue") String identityValue) {
        return userApplicationService.getUserLoginCredential(tenantNo, identityType, identityValue);
    }

    @Operation(summary = "当前用户修改密码")
    @PostMapping("/users/{userId}/password/change")
    public void changePassword(@RequestParam("tenantNo") String tenantNo,
                               @PathVariable Long userId,
                               @RequestBody UserPasswordChangeDTO request) {
        userApplicationService.changePassword(tenantNo, userId, request.getOldPassword(), request.getNewPassword());
    }

    @Operation(summary = "按租户编号查询租户")
    @GetMapping("/tenants/{tenantNo}")
    public TenantDTO getTenant(@PathVariable String tenantNo) {
        return userApplicationService.getTenantByTenantNo(tenantNo);
    }

    @Operation(summary = "按部门 ID 查询部门")
    @GetMapping("/departments/{departmentId}")
    public DepartmentDTO getDepartmentById(@RequestParam("tenantNo") String tenantNo, @PathVariable Long departmentId) {
        return departmentApplicationService.getDepartmentById(tenantNo, departmentId);
    }

    @Operation(summary = "按部门编码查询部门")
    @GetMapping("/departments/code/{departmentCode}")
    public DepartmentDTO getDepartmentByCode(@RequestParam("tenantNo") String tenantNo, @PathVariable String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(tenantNo, departmentCode);
    }

    @Operation(summary = "批量查询部门")
    @GetMapping("/departments")
    public List<DepartmentDTO> listDepartmentsByIds(@RequestParam("tenantNo") String tenantNo,
                                                    @RequestParam("departmentIds") Set<Long> departmentIds) {
        return departmentApplicationService.listDepartmentsByIds(tenantNo, departmentIds);
    }

    @Operation(summary = "按角色 ID 查询角色")
    @GetMapping("/roles/{roleId}")
    public RoleDTO getRoleById(@RequestParam("tenantNo") String tenantNo, @PathVariable Long roleId) {
        return roleApplicationService.getRoleById(tenantNo, roleId);
    }

    @Operation(summary = "查询用户角色列表")
    @GetMapping("/roles")
    public List<RoleDTO> getRolesByUserId(@RequestParam("tenantNo") String tenantNo, @RequestParam("userId") Long userId) {
        return roleApplicationService.getRolesByUserId(tenantNo, userId);
    }

    @Operation(summary = "查询用户菜单树")
    @GetMapping("/permissions/menus")
    public List<UserMenuTreeDTO> getUserMenuTree(@RequestParam("tenantNo") String tenantNo, @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserMenuTree(tenantNo, userId);
    }

    @Operation(summary = "查询用户权限码")
    @GetMapping("/permissions/codes")
    public Set<String> getUserPermissionCodes(@RequestParam("tenantNo") String tenantNo, @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserPermissionCodes(tenantNo, userId);
    }

    @Operation(summary = "查询用户数据权限范围")
    @GetMapping("/permissions/data-scope")
    public UserDataScopeDTO getUserDataScope(@RequestParam("tenantNo") String tenantNo, @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserDataScope(tenantNo, userId);
    }
}
