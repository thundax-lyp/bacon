package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.api.facade.RoleReadFacade;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/providers/upms")
public class UpmsProviderController {

    private final UserReadFacade userReadFacade;
    private final DepartmentReadFacade departmentReadFacade;
    private final RoleReadFacade roleReadFacade;
    private final PermissionReadFacade permissionReadFacade;

    public UpmsProviderController(UserReadFacade userReadFacade, DepartmentReadFacade departmentReadFacade,
                                  RoleReadFacade roleReadFacade, PermissionReadFacade permissionReadFacade) {
        this.userReadFacade = userReadFacade;
        this.departmentReadFacade = departmentReadFacade;
        this.roleReadFacade = roleReadFacade;
        this.permissionReadFacade = permissionReadFacade;
    }

    @GetMapping("/users/{userId}")
    public UserDTO getUserById(@RequestParam("tenantId") Long tenantId, @PathVariable Long userId) {
        return userReadFacade.getUserById(tenantId, userId);
    }

    @GetMapping("/user-identities")
    public UserIdentityDTO getUserIdentity(@RequestParam("tenantId") Long tenantId,
                                           @RequestParam("identityType") String identityType,
                                           @RequestParam("identityValue") String identityValue) {
        return userReadFacade.getUserIdentity(tenantId, identityType, identityValue);
    }

    @GetMapping("/tenants/{tenantId}")
    public TenantDTO getTenant(@PathVariable Long tenantId) {
        return userReadFacade.getTenantByTenantId(tenantId);
    }

    @GetMapping("/departments/{departmentId}")
    public DepartmentDTO getDepartmentById(@RequestParam("tenantId") Long tenantId, @PathVariable Long departmentId) {
        return departmentReadFacade.getDepartmentById(tenantId, departmentId);
    }

    @GetMapping("/departments/code/{departmentCode}")
    public DepartmentDTO getDepartmentByCode(@RequestParam("tenantId") Long tenantId, @PathVariable String departmentCode) {
        return departmentReadFacade.getDepartmentByCode(tenantId, departmentCode);
    }

    @GetMapping("/departments")
    public List<DepartmentDTO> listDepartmentsByIds(@RequestParam("tenantId") Long tenantId,
                                                    @RequestParam("departmentIds") Set<Long> departmentIds) {
        return departmentReadFacade.listDepartmentsByIds(tenantId, departmentIds);
    }

    @GetMapping("/roles/{roleId}")
    public RoleDTO getRoleById(@RequestParam("tenantId") Long tenantId, @PathVariable Long roleId) {
        return roleReadFacade.getRoleById(tenantId, roleId);
    }

    @GetMapping("/roles")
    public List<RoleDTO> getRolesByUserId(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return roleReadFacade.getRolesByUserId(tenantId, userId);
    }

    @GetMapping("/permissions/menus")
    public List<UserMenuTreeDTO> getUserMenuTree(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return permissionReadFacade.getUserMenuTree(tenantId, userId);
    }

    @GetMapping("/permissions/codes")
    public Set<String> getUserPermissionCodes(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return permissionReadFacade.getUserPermissionCodes(tenantId, userId);
    }

    @GetMapping("/permissions/data-scope")
    public UserDataScopeDTO getUserDataScope(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return permissionReadFacade.getUserDataScope(tenantId, userId);
    }
}
