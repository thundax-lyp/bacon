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
    public UserDTO getUserById(@RequestParam("tenantId") String tenantId, @PathVariable String userId) {
        return userApplicationService.getUserById(tenantId, userId);
    }

    @Operation(summary = "按身份标识查询用户身份")
    @GetMapping("/user-identities")
    public UserIdentityDTO getUserIdentity(@RequestParam("tenantId") String tenantId,
                                           @RequestParam("identityType") String identityType,
                                           @RequestParam("identityValue") String identityValue) {
        return userApplicationService.getUserIdentity(tenantId, identityType, identityValue);
    }

    @Operation(summary = "按身份标识查询用户登录凭据")
    @GetMapping("/user-credentials")
    public UserLoginCredentialDTO getUserLoginCredential(@RequestParam("tenantId") String tenantId,
                                                         @RequestParam("identityType") String identityType,
                                                         @RequestParam("identityValue") String identityValue) {
        return userApplicationService.getUserLoginCredential(tenantId, identityType, identityValue);
    }

    @Operation(summary = "当前用户修改密码")
    @PostMapping("/users/{userId}/password/change")
    public void changePassword(@RequestParam("tenantId") String tenantId,
                               @PathVariable String userId,
                               @RequestBody UserPasswordChangeDTO request) {
        userApplicationService.changePassword(tenantId, userId, request.getOldPassword(), request.getNewPassword());
    }

    @Operation(summary = "按租户编号查询租户")
    @GetMapping("/tenants/{tenantId}")
    public TenantDTO getTenant(@PathVariable String tenantId) {
        return userApplicationService.getTenantByTenantId(tenantId);
    }

    @Operation(summary = "按部门 ID 查询部门")
    @GetMapping("/departments/{departmentId}")
    public DepartmentDTO getDepartmentById(@RequestParam("tenantId") String tenantId, @PathVariable String departmentId) {
        return departmentApplicationService.getDepartmentById(tenantId, departmentId);
    }

    @Operation(summary = "按部门编码查询部门")
    @GetMapping("/departments/code/{departmentCode}")
    public DepartmentDTO getDepartmentByCode(@RequestParam("tenantId") String tenantId, @PathVariable String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(tenantId, departmentCode);
    }

    @Operation(summary = "批量查询部门")
    @GetMapping("/departments")
    public List<DepartmentDTO> listDepartmentsByIds(@RequestParam("tenantId") String tenantId,
                                                    @RequestParam("departmentIds") Set<String> departmentIds) {
        return departmentApplicationService.listDepartmentsByIds(tenantId, departmentIds);
    }

    @Operation(summary = "按角色 ID 查询角色")
    @GetMapping("/roles/{roleId}")
    public RoleDTO getRoleById(@RequestParam("tenantId") String tenantId, @PathVariable String roleId) {
        return roleApplicationService.getRoleById(tenantId, roleId);
    }

    @Operation(summary = "查询用户角色列表")
    @GetMapping("/roles")
    public List<RoleDTO> getRolesByUserId(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId) {
        return roleApplicationService.getRolesByUserId(tenantId, userId);
    }

    @Operation(summary = "查询用户菜单树")
    @GetMapping("/permissions/menus")
    public List<UserMenuTreeDTO> getUserMenuTree(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId) {
        return permissionQueryService.getUserMenuTree(tenantId, userId);
    }

    @Operation(summary = "查询用户权限码")
    @GetMapping("/permissions/codes")
    public Set<String> getUserPermissionCodes(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId) {
        return permissionQueryService.getUserPermissionCodes(tenantId, userId);
    }

    @Operation(summary = "查询用户数据权限范围")
    @GetMapping("/permissions/data-scope")
    public UserDataScopeDTO getUserDataScope(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId) {
        return permissionQueryService.getUserDataScope(tenantId, userId);
    }
}
