package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.service.UserApplicationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/upms/users")
public class UserQueryController {

    private final UserApplicationService userApplicationService;

    public UserQueryController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @GetMapping("/{userId}")
    public UserDTO getUserById(@RequestParam("tenantId") Long tenantId, @PathVariable Long userId) {
        return userApplicationService.getUserById(tenantId, userId);
    }

    @GetMapping("/identity")
    public UserIdentityDTO getUserIdentity(@RequestParam("tenantId") Long tenantId,
                                           @RequestParam("identityType") String identityType,
                                           @RequestParam("identityValue") String identityValue) {
        return userApplicationService.getUserIdentity(tenantId, identityType, identityValue);
    }

    @GetMapping("/tenants/{tenantId}")
    public TenantDTO getTenant(@PathVariable Long tenantId) {
        return userApplicationService.getTenantByTenantId(tenantId);
    }
}
