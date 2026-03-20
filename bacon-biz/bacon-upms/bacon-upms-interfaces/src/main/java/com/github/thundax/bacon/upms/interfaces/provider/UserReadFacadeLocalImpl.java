package com.github.thundax.bacon.upms.interfaces.provider;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import com.github.thundax.bacon.upms.application.service.UserApplicationService;
import org.springframework.stereotype.Component;

@Component
public class UserReadFacadeLocalImpl implements UserReadFacade {

    private final UserApplicationService userApplicationService;

    public UserReadFacadeLocalImpl(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Override
    public UserDTO getUserById(Long tenantId, Long userId) {
        return userApplicationService.getUserById(tenantId, userId);
    }

    @Override
    public UserIdentityDTO getUserIdentity(Long tenantId, String identityType, String identityValue) {
        return userApplicationService.getUserIdentity(tenantId, identityType, identityValue);
    }

    @Override
    public TenantDTO getTenantByTenantId(Long tenantId) {
        return userApplicationService.getTenantByTenantId(tenantId);
    }
}
