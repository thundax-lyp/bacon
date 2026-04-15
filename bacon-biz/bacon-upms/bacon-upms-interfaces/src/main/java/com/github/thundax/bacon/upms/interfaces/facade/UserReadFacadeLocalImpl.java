package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import com.github.thundax.bacon.upms.application.command.UserApplicationService;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class UserReadFacadeLocalImpl implements UserReadFacade {

    private final UserApplicationService userApplicationService;

    public UserReadFacadeLocalImpl(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Override
    public UserDTO getUserById(@NonNull UserId userId) {
        BaconContextHolder.requireTenantId();
        return userApplicationService.getUserById(userId);
    }

    @Override
    public UserIdentityDTO getUserIdentity(UserIdentityType identityType, String identityValue) {
        BaconContextHolder.requireTenantId();
        return userApplicationService.getUserIdentity(identityType, identityValue);
    }

    @Override
    public UserLoginCredentialDTO getUserLoginCredential(UserIdentityType identityType, String identityValue) {
        BaconContextHolder.requireTenantId();
        return userApplicationService.getUserLoginCredential(identityType, identityValue);
    }

    @Override
    public TenantDTO getTenantByTenantId() {
        BaconContextHolder.requireTenantId();
        return userApplicationService.getTenantByTenantId(TenantId.of(BaconContextHolder.requireTenantId()));
    }
}
