package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import com.github.thundax.bacon.upms.application.command.UserApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class UserReadFacadeLocalImpl implements UserReadFacade {

    private final UserApplicationService userApplicationService;

    public UserReadFacadeLocalImpl(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Override
    public UserDTO getUserById(String tenantNo, String userId) {
        return userApplicationService.getUserById(tenantNo, userId);
    }

    @Override
    public UserIdentityDTO getUserIdentity(String tenantNo, String identityType, String identityValue) {
        return userApplicationService.getUserIdentity(tenantNo, identityType, identityValue);
    }

    @Override
    public UserLoginCredentialDTO getUserLoginCredential(String tenantNo, String identityType, String identityValue) {
        return userApplicationService.getUserLoginCredential(tenantNo, identityType, identityValue);
    }

    @Override
    public TenantDTO getTenantByTenantNo(String tenantNo) {
        return userApplicationService.getTenantByTenantNo(tenantNo);
    }
}
