package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.request.UserCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.UserCredentialFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;

public interface UserCredentialReadFacade {

    UserIdentityFacadeResponse getUserIdentity(UserIdentityGetFacadeRequest request);

    UserCredentialFacadeResponse getUserCredential(UserCredentialGetFacadeRequest request);
}
