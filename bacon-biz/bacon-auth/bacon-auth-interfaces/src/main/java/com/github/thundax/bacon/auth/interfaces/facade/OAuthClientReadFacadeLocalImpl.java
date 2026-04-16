package com.github.thundax.bacon.auth.interfaces.facade;

import com.github.thundax.bacon.auth.api.facade.OAuthClientReadFacade;
import com.github.thundax.bacon.auth.api.request.OAuthClientGetFacadeRequest;
import com.github.thundax.bacon.auth.api.response.OAuthClientFacadeResponse;
import com.github.thundax.bacon.auth.application.query.OAuth2ClientApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class OAuthClientReadFacadeLocalImpl implements OAuthClientReadFacade {

    private final OAuth2ClientApplicationService oAuth2ClientApplicationService;

    public OAuthClientReadFacadeLocalImpl(OAuth2ClientApplicationService oAuth2ClientApplicationService) {
        this.oAuth2ClientApplicationService = oAuth2ClientApplicationService;
    }

    @Override
    public OAuthClientFacadeResponse getClientByClientId(OAuthClientGetFacadeRequest request) {
        return OAuthClientFacadeResponse.from(oAuth2ClientApplicationService.getClientByClientId(request.getClientId()));
    }
}
