package com.github.thundax.bacon.auth.interfaces.facade;

import com.github.thundax.bacon.auth.api.facade.OAuthClientReadFacade;
import com.github.thundax.bacon.auth.api.request.OAuthClientGetFacadeRequest;
import com.github.thundax.bacon.auth.api.response.OAuthClientFacadeResponse;
import com.github.thundax.bacon.auth.application.query.OAuthClientQueryApplicationService;
import com.github.thundax.bacon.auth.application.query.OAuthClientQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class OAuthClientReadFacadeLocalImpl implements OAuthClientReadFacade {

    private final OAuthClientQueryApplicationService oAuth2ClientQueryApplicationService;

    public OAuthClientReadFacadeLocalImpl(OAuthClientQueryApplicationService oAuth2ClientQueryApplicationService) {
        this.oAuth2ClientQueryApplicationService = oAuth2ClientQueryApplicationService;
    }

    @Override
    public OAuthClientFacadeResponse getClientByClientId(OAuthClientGetFacadeRequest request) {
        return OAuthClientFacadeResponse.from(
                oAuth2ClientQueryApplicationService.getClientByClientId(new OAuthClientQuery(request.getClientId())));
    }
}
