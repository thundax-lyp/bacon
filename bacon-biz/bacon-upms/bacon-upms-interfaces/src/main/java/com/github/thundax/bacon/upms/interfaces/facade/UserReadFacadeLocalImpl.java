package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import com.github.thundax.bacon.upms.api.request.UserGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserLoginCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserLoginCredentialFacadeResponse;
import com.github.thundax.bacon.upms.application.command.UserApplicationService;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
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
    public UserFacadeResponse getUserById(UserGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return UserFacadeResponse.from(userApplicationService.getUserById(UserId.of(request.getUserId())));
    }

    @Override
    public UserIdentityFacadeResponse getUserIdentity(UserIdentityGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return UserIdentityFacadeResponse.from(userApplicationService.getUserIdentity(
                UserIdentityType.from(request.getIdentityType()), request.getIdentityValue()));
    }

    @Override
    public UserLoginCredentialFacadeResponse getUserLoginCredential(UserLoginCredentialGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return UserLoginCredentialFacadeResponse.from(userApplicationService.getUserLoginCredential(
                UserIdentityType.from(request.getIdentityType()), request.getIdentityValue()));
    }

    @Override
    public TenantFacadeResponse getTenantByTenantId() {
        BaconContextHolder.requireTenantId();
        return TenantFacadeResponse.from(
                userApplicationService.getTenantByTenantId(BaconIdContextHelper.requireTenantId()));
    }
}
