package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.upms.api.dto.*;
import com.github.thundax.bacon.upms.application.service.DepartmentApplicationService;
import com.github.thundax.bacon.upms.application.service.PermissionQueryService;
import com.github.thundax.bacon.upms.application.service.RoleApplicationService;
import com.github.thundax.bacon.upms.application.service.UserApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/providers/upms")
public class UpmsProviderController {

    private final UserApplicationService userApplicationService;
    private final DepartmentApplicationService departmentApplicationService;
    private final RoleApplicationService roleApplicationService;
    private final PermissionQueryService permissionQueryService;

    public UpmsProviderController(UserApplicationService userApplicationService,
                                  DepartmentApplicationService departmentApplicationService,
                                  RoleApplicationService roleApplicationService,
                                  PermissionQueryService permissionQueryService) {
        this.userApplicationService = userApplicationService;
        this.departmentApplicationService = departmentApplicationService;
        this.roleApplicationService = roleApplicationService;
        this.permissionQueryService = permissionQueryService;
    }

    @GetMapping("/users/{userId}")
    public UserDTO getUserById(@RequestParam("tenantId") Long tenantId, @PathVariable Long userId) {
        return userApplicationService.getUserById(tenantId, userId);
    }

    @GetMapping("/user-identities")
    public UserIdentityDTO getUserIdentity(@RequestParam("tenantId") Long tenantId,
                                           @RequestParam("identityType") String identityType,
                                           @RequestParam("identityValue") String identityValue) {
        return userApplicationService.getUserIdentity(tenantId, identityType, identityValue);
    }

    @GetMapping("/tenants/{tenantId}")
    public TenantDTO getTenant(@PathVariable Long tenantId) {
        return userApplicationService.getTenantByTenantId(tenantId);
    }

    @GetMapping("/departments/{departmentId}")
    public DepartmentDTO getDepartmentById(@RequestParam("tenantId") Long tenantId, @PathVariable Long departmentId) {
        return departmentApplicationService.getDepartmentById(tenantId, departmentId);
    }

    @GetMapping("/departments/code/{departmentCode}")
    public DepartmentDTO getDepartmentByCode(@RequestParam("tenantId") Long tenantId, @PathVariable String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(tenantId, departmentCode);
    }

    @GetMapping("/departments")
    public List<DepartmentDTO> listDepartmentsByIds(@RequestParam("tenantId") Long tenantId,
                                                    @RequestParam("departmentIds") Set<Long> departmentIds) {
        return departmentApplicationService.listDepartmentsByIds(tenantId, departmentIds);
    }

    @GetMapping("/roles/{roleId}")
    public RoleDTO getRoleById(@RequestParam("tenantId") Long tenantId, @PathVariable Long roleId) {
        return roleApplicationService.getRoleById(tenantId, roleId);
    }

    @GetMapping("/roles")
    public List<RoleDTO> getRolesByUserId(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return roleApplicationService.getRolesByUserId(tenantId, userId);
    }

    @GetMapping("/permissions/menus")
    public List<UserMenuTreeDTO> getUserMenuTree(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserMenuTree(tenantId, userId);
    }

    @GetMapping("/permissions/codes")
    public Set<String> getUserPermissionCodes(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserPermissionCodes(tenantId, userId);
    }

    @GetMapping("/permissions/data-scope")
    public UserDataScopeDTO getUserDataScope(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return permissionQueryService.getUserDataScope(tenantId, userId);
    }
}
