package com.github.thundax.bacon.auth.interfaces.provider;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.facade.OAuthClientReadFacade;
import com.github.thundax.bacon.auth.application.service.OAuth2ClientApplicationService;
import org.springframework.stereotype.Component;

@Component
public class OAuthClientReadFacadeLocalImpl implements OAuthClientReadFacade {

    private final OAuth2ClientApplicationService oAuth2ClientApplicationService;

    public OAuthClientReadFacadeLocalImpl(OAuth2ClientApplicationService oAuth2ClientApplicationService) {
        this.oAuth2ClientApplicationService = oAuth2ClientApplicationService;
    }

    @Override
    public OAuthClientDTO getClientByClientId(String clientId) {
        return oAuth2ClientApplicationService.getClientByClientId(clientId);
    }
}
