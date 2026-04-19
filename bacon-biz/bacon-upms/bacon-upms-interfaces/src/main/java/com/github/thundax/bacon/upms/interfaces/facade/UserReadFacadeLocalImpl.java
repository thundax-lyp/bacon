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
import com.github.thundax.bacon.upms.api.response.UserIdentityDetailFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserLoginCredentialDetailFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserLoginCredentialFacadeResponse;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import com.github.thundax.bacon.upms.application.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class UserReadFacadeLocalImpl implements UserReadFacade {

    private final UserQueryApplicationService userQueryApplicationService;

    public UserReadFacadeLocalImpl(UserQueryApplicationService userQueryApplicationService) {
        this.userQueryApplicationService = userQueryApplicationService;
    }

    @Override
    public UserFacadeResponse getUserById(UserGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return UserFacadeResponse.from(userQueryApplicationService.getUserById(UserId.of(request.getUserId())));
    }

    @Override
    public UserIdentityFacadeResponse getUserIdentity(UserIdentityGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        UserIdentityDTO userIdentity = userQueryApplicationService.getUserIdentity(
                UserIdentityType.from(request.getIdentityType()), request.getIdentityValue());
        return UserIdentityFacadeResponse.from(new UserIdentityDetailFacadeResponse(
                userIdentity.getId(),
                userIdentity.getUserId(),
                userIdentity.getIdentityType(),
                userIdentity.getIdentityValue(),
                userIdentity.getStatus()));
    }

    @Override
    public UserLoginCredentialFacadeResponse getUserLoginCredential(UserLoginCredentialGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        UserLoginCredentialDTO credential = userQueryApplicationService.getUserLoginCredential(
                UserIdentityType.from(request.getIdentityType()), request.getIdentityValue());
        return UserLoginCredentialFacadeResponse.from(new UserLoginCredentialDetailFacadeResponse(
                credential.getUserId(),
                credential.getIdentityId(),
                credential.getAccount(),
                credential.getPhone(),
                credential.getIdentityType(),
                credential.getIdentityValue(),
                credential.getIdentityStatus(),
                credential.getCredentialId(),
                credential.getCredentialType(),
                credential.getCredentialStatus(),
                credential.isNeedChangePassword(),
                credential.getCredentialExpiresAt(),
                credential.getLockedUntil(),
                credential.isMfaRequired(),
                credential.getSecondFactorTypes(),
                credential.getStatus(),
                credential.getPasswordHash()));
    }

    @Override
    public TenantFacadeResponse getTenantByTenantId() {
        BaconContextHolder.requireTenantId();
        return TenantFacadeResponse.from(
                userQueryApplicationService.getTenantByTenantId(BaconIdContextHelper.requireTenantId()));
    }
}
