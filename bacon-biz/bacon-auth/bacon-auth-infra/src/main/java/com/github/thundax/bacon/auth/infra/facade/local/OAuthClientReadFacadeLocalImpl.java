package com.github.thundax.bacon.auth.infra.facade.local;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.facade.OAuthClientReadFacade;
import com.github.thundax.bacon.auth.application.service.OAuth2ClientApplicationService;
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
    public OAuthClientDTO getClientByClientId(String clientId) {
        return oAuth2ClientApplicationService.getClientByClientId(clientId);
    }
}
