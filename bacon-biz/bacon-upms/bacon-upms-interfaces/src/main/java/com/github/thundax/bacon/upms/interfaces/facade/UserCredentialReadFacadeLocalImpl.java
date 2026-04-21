package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.upms.api.facade.UserCredentialReadFacade;
import com.github.thundax.bacon.upms.api.request.UserCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.UserCredentialFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
import com.github.thundax.bacon.upms.interfaces.assembler.UserInterfaceAssembler;
import com.github.thundax.bacon.upms.application.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class UserCredentialReadFacadeLocalImpl implements UserCredentialReadFacade {

    private final UserQueryApplicationService userQueryApplicationService;

    public UserCredentialReadFacadeLocalImpl(UserQueryApplicationService userQueryApplicationService) {
        this.userQueryApplicationService = userQueryApplicationService;
    }

    @Override
    public UserIdentityFacadeResponse getUserIdentity(UserIdentityGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        UserIdentityDTO userIdentity = userQueryApplicationService.getUserIdentity(
                UserInterfaceAssembler.toIdentityQuery(request));
        return UserInterfaceAssembler.toIdentityFacadeResponse(userIdentity);
    }

    @Override
    public UserCredentialFacadeResponse getUserCredential(UserCredentialGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        UserLoginCredentialDTO credential = userQueryApplicationService.getUserLoginCredential(
                UserInterfaceAssembler.toLoginCredentialQuery(request));
        return UserInterfaceAssembler.toCredentialFacadeResponse(credential);
    }
}
