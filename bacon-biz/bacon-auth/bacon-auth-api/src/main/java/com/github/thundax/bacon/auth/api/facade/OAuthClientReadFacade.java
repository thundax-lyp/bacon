package com.github.thundax.bacon.auth.api.facade;

import com.github.thundax.bacon.auth.api.request.OAuthClientGetFacadeRequest;
import com.github.thundax.bacon.auth.api.response.OAuthClientFacadeResponse;

public interface OAuthClientReadFacade {

    OAuthClientFacadeResponse getClientByClientId(OAuthClientGetFacadeRequest request);
}
