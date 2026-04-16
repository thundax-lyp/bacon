package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.request.UserGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserLoginCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserLoginCredentialFacadeResponse;

public interface UserReadFacade {

    UserFacadeResponse getUserById(UserGetFacadeRequest request);

    UserIdentityFacadeResponse getUserIdentity(UserIdentityGetFacadeRequest request);

    UserLoginCredentialFacadeResponse getUserLoginCredential(UserLoginCredentialGetFacadeRequest request);

    TenantFacadeResponse getTenantByTenantId();
}
