package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.service.RoleApplicationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/roles")
public class RoleQueryController {

    private final RoleApplicationService roleApplicationService;

    public RoleQueryController(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    @GetMapping("/{roleId}")
    public RoleDTO getRoleById(@RequestParam("tenantId") Long tenantId, @PathVariable Long roleId) {
        return roleApplicationService.getRoleById(tenantId, roleId);
    }

    @GetMapping
    public List<RoleDTO> getRolesByUserId(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId) {
        return roleApplicationService.getRolesByUserId(tenantId, userId);
    }
}
